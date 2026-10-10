import makeWASocket, { useMultiFileAuthState, DisconnectReason, Browsers } from '@whiskeysockets/baileys'
import express from 'express'
import pino from 'pino'

const app = express(); app.use(express.json())
const COOL = 5 * 60 * 1000
const socks = new Map(), last = new Map()
const sleep = ms => new Promise(r => setTimeout(r, ms))
const clean = s => String(s || '').toLowerCase().replace(/[^a-z0-9_]/g, '').slice(0, 80)

async function start(id) {
  const { state, saveCreds } = await useMultiFileAuthState('auth/' + id)
  const sock = makeWASocket({ auth: state, browser: Browsers.ubuntu('Chrome'), logger: pino({ level: 'silent' }) })
  sock.ready = false; sock.registered = !!state.creds.registered
  sock.ev.on('creds.update', saveCreds)
  sock.ev.on('connection.update', ({ connection, lastDisconnect }) => {
    if (connection === 'open') { sock.ready = true; sock.registered = true }
    if (connection === 'close') {
      sock.ready = false
      const code = lastDisconnect?.error?.output?.statusCode
      socks.delete(id)
      if (code !== DisconnectReason.loggedOut) start(id).catch(() => {})
    }
  })
  socks.set(id, sock); return sock
}

app.post('/pair', async (req, res) => {
  try {
    const id = clean(req.body.id), number = String(req.body.number || '').replace(/\D/g, '')
    if (!id || number.length < 8) return res.status(400).json({ error: 'Data tidak valid' })
    const sock = socks.get(id) || await start(id)
    if (sock.registered) return res.json({ code: '', paired: true })
    await sleep(2500)
    res.json({ code: await sock.requestPairingCode(number) })
  } catch (e) { res.status(500).json({ error: 'Gagal membuat kode: ' + e.message }) }
})

app.post('/status', async (req, res) => {
  const id = clean(req.body.id)
  const sock = socks.get(id)
  res.json({ paired: !!(sock && sock.ready) })
})

app.post('/send', async (req, res) => {
  try {
    const id = clean(req.body.id), to = String(req.body.to || '').replace(/\D/g, ''), text = String(req.body.text || '').slice(0, 1000)
    if (!id || to.length < 8 || !text) return res.status(400).json({ error: 'Data tidak valid' })
    const remain = COOL - (Date.now() - (last.get(id) || 0))
    if (remain > 0) return res.status(429).json({ error: 'Cooldown', remainMs: remain })
    let sock = socks.get(id) || await start(id)
    for (let i = 0; i < 20 && !sock.ready; i++) await sleep(1000)
    if (!sock.ready) return res.status(409).json({ error: 'WhatsApp belum terhubung, lakukan pairing ulang' })
    last.set(id, Date.now())
    await sock.sendMessage(to + '@s.whatsapp.net', { text })
    res.json({ ok: true })
  } catch (e) { res.status(500).json({ error: e.message }) }
})

app.listen(process.env.PORT || 3000, () => console.log('gunzdev backend jalan'))
