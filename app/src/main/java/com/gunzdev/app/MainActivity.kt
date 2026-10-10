package com.gunzdev.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.sin

val Cyan = Color(0xFF00E5FF)
val Navy = Color(0xFF0A0A28)
val Violet = Color(0xFF6C3BFF)
const val COOLDOWN = 5 * 60 * 1000L

object Api {
    fun call(path: String, body: JSONObject): Pair<Int, JSONObject> {
        val c = URL(BuildConfig.BACKEND_URL + path).openConnection() as HttpURLConnection
        c.connectTimeout = 20000; c.readTimeout = 45000
        c.requestMethod = "POST"; c.doOutput = true
        c.setRequestProperty("Content-Type", "application/json")
        c.outputStream.use { it.write(body.toString().toByteArray()) }
        val code = c.responseCode
        val txt = (if (code < 400) c.inputStream else c.errorStream).bufferedReader().readText()
        return code to (try { JSONObject(txt) } catch (e: Exception) { JSONObject() })
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, background = Navy, surface = Navy)) { App() } }
    }
}

enum class Stage { LOADING, LOGIN, PAIR, MAIN }

@Composable
fun App() {
    val ctx = LocalContext()
    val sp = remember { ctx.getSharedPreferences("gdev", Context.MODE_PRIVATE) }
    var stage by remember { mutableStateOf(Stage.LOADING) }
    var email by remember { mutableStateOf(sp.getString("email", "") ?: "") }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Navy, Color(0xFF1B1464), Color(0xFF0B0B30))))) {
        Stars()
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            when (stage) {
                Stage.LOADING -> LoadingScreen {
                    stage = if (email.isEmpty()) Stage.LOGIN
                    else if (!sp.getBoolean("paired", false)) Stage.PAIR else Stage.MAIN
                }
                Stage.LOGIN -> LoginScreen { e ->
                    email = e; sp.edit().putString("email", e).apply(); stage = Stage.PAIR
                }
                Stage.PAIR -> PairScreen(email) { sp.edit().putBoolean("paired", true).apply(); stage = Stage.MAIN }
                Stage.MAIN -> MainScreen(email, sp)
            }
        }
    }
}

@Composable fun LocalContext() = androidx.compose.ui.platform.LocalContext.current

@Composable
fun Stars() {
    val t = rememberInfiniteTransition(label = "s")
    val a by t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "a")
    Canvas(Modifier.fillMaxSize()) {
        for (i in 0 until 70) {
            val x = ((i * 977) % 1000) / 1000f * size.width
            val y = ((i * 613) % 1000) / 1000f * size.height
            val al = if (i % 2 == 0) a else 1.3f - a
            drawCircle(Color.White.copy(alpha = al.coerceIn(0.1f, 1f)), radius = 1.2f + (i % 3), center = Offset(x, y))
        }
    }
}

@Composable
fun Astronaut(size: Int = 200) {
    val t = rememberInfiniteTransition(label = "f")
    val p by t.animateFloat(0f, 6.28f, infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "p")
    Canvas(Modifier.size(size.dp)) {
        val w = this.size.width
        val dy = sin(p) * 12f
        val c = Offset(w / 2, w / 2 + dy)
        drawRoundRect(Color(0xFF9AA5C8), Offset(c.x - w * .3f, c.y - w * .05f), Size(w * .6f, w * .42f), CornerRadius(30f))
        drawRoundRect(Color.White, Offset(c.x - w * .22f, c.y), Size(w * .44f, w * .4f), CornerRadius(40f))
        drawRoundRect(Color.White, Offset(c.x - w * .36f, c.y + w * .03f), Size(w * .13f, w * .3f), CornerRadius(30f))
        drawRoundRect(Color.White, Offset(c.x + w * .23f, c.y + w * .03f), Size(w * .13f, w * .3f), CornerRadius(30f))
        drawRoundRect(Color(0xFFDDE3F5), Offset(c.x - w * .2f, c.y + w * .36f), Size(w * .15f, w * .16f), CornerRadius(20f))
        drawRoundRect(Color(0xFFDDE3F5), Offset(c.x + w * .05f, c.y + w * .36f), Size(w * .15f, w * .16f), CornerRadius(20f))
        drawCircle(Color.White, w * .23f, Offset(c.x, c.y - w * .08f))
        drawRoundRect(Color(0xFF14143C), Offset(c.x - w * .17f, c.y - w * .2f), Size(w * .34f, w * .22f), CornerRadius(50f))
        drawCircle(Cyan.copy(alpha = .6f), w * .035f, Offset(c.x - w * .08f, c.y - w * .14f))
        drawCircle(Violet, w * .035f, Offset(c.x, c.y + w * .17f))
    }
}

@Composable
fun LoadingScreen(onNext: () -> Unit) {
    val prog = remember { Animatable(0f) }
    LaunchedEffect(Unit) { prog.animateTo(1f, tween(3000, easing = FastOutSlowInEasing)) }
    Spacer(Modifier.weight(1f))
    Astronaut(220)
    Spacer(Modifier.height(8.dp))
    Image(painterResource(R.mipmap.ic_launcher), null, Modifier.size(84.dp).clip(RoundedCornerShape(22.dp)))
    Spacer(Modifier.height(12.dp))
    Text("gunzdev", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Cyan, letterSpacing = 4.sp)
    Text("Jelajahi galaksi pesanmu", color = Color.White.copy(.7f))
    Spacer(Modifier.height(28.dp))
    LinearProgressIndicator(progress = { prog.value }, Modifier.fillMaxWidth(.7f).height(8.dp).clip(CircleShape),
        color = Cyan, trackColor = Color.White.copy(.15f))
    Text("${(prog.value * 100).toInt()}%", color = Color.White.copy(.7f), modifier = Modifier.padding(top = 6.dp))
    Spacer(Modifier.weight(1f))
    if (prog.value >= 1f) {
        Button(onClick = onNext, shape = CircleShape, modifier = Modifier.size(72.dp),
            contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = Cyan)) {
            Text("➜", fontSize = 30.sp, color = Navy)
        }
    } else Spacer(Modifier.height(72.dp))
    Spacer(Modifier.height(16.dp))
}

@Composable
fun LoginScreen(onDone: (String) -> Unit) {
    val ctx = LocalContext()
    var err by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        try {
            val acc = GoogleSignIn.getSignedInAccountFromIntent(r.data).getResult(ApiException::class.java)
            onDone(acc.email ?: "")
        } catch (e: ApiException) {
            err = "Login gagal (kode ${e.statusCode}). Kode 10 = SHA-1 belum didaftarkan di Google Cloud."
        } catch (e: Exception) { err = "Login dibatalkan." }
    }
    Spacer(Modifier.weight(1f))
    Astronaut(180)
    Text("Selamat datang, Astronot!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
    Text("Hubungkan akun Google untuk melanjutkan. Login wajib.", color = Color.White.copy(.7f),
        textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
    Button(onClick = {
        val o = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        val cl = GoogleSignIn.getClient(ctx, o)
        cl.signOut().addOnCompleteListener { launcher.launch(cl.signInIntent) }
    }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
        Text("G  Masuk dengan Google", fontWeight = FontWeight.Bold)
    }
    if (err.isNotEmpty()) Text(err, color = Color(0xFFFF6B6B), modifier = Modifier.padding(top = 12.dp), textAlign = TextAlign.Center)
    Spacer(Modifier.weight(1f))
}

fun sid(email: String) = email.lowercase().replace(Regex("[^a-z0-9]"), "_")
fun normNum(n: String): String { val d = n.filter { it.isDigit() }; return if (d.startsWith("0")) "62" + d.drop(1) else d }

@Composable
fun PairScreen(email: String, onPaired: () -> Unit) {
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    Column(Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        Astronaut(140)
        Text("Pairing WhatsApp", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Masukkan nomor WhatsApp kamu (contoh 628123456789), lalu masukkan kode di WhatsApp.",
            color = Color.White.copy(.7f), textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 10.dp))
        OutlinedTextField(phone, { phone = it }, label = { Text("Nomor WhatsApp kamu") }, singleLine = true,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            busy = true; msg = "Meminta kode..."
            scope.launch {
                val r = withContext(Dispatchers.IO) {
                    try { Api.call("/pair", JSONObject().put("id", sid(email)).put("number", normNum(phone))) }
                    catch (e: Exception) { 0 to JSONObject().put("error", "Tidak bisa terhubung ke server") }
                }
                busy = false
                if (r.first == 200) { code = r.second.optString("code"); msg = "Buka WhatsApp > Perangkat tertaut > Tautkan dengan nomor telepon, lalu masukkan kode ini." }
                else msg = r.second.optString("error", "Gagal (${r.first})")
            }
        }, enabled = !busy && phone.length > 6, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
            Text("Dapatkan Kode Pairing")
        }
        if (code.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(code, fontSize = 38.sp, fontWeight = FontWeight.Black, color = Cyan, letterSpacing = 6.sp)
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                busy = true; msg = "Menunggu tautan..."
                scope.launch {
                    var ok = false
                    repeat(20) {
                        if (!ok) {
                            ok = withContext(Dispatchers.IO) {
                                try { Api.call("/status", JSONObject().put("id", sid(email))).second.optBoolean("paired") } catch (e: Exception) { false }
                            }
                            if (!ok) delay(3000)
                        }
                    }
                    busy = false
                    if (ok) onPaired() else msg = "Belum terhubung. Pastikan kode sudah dimasukkan lalu coba lagi."
                }
            }, enabled = !busy, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Saya sudah memasukkan kode")
            }
        }
        if (msg.isNotEmpty()) Text(msg, color = Color.White.copy(.8f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
    }
}

val templates = listOf(
    "💖 Jatuh cinta" to "Hai, sebenarnya sudah lama aku menyimpan perasaan ini. Aku jatuh cinta sama kamu. Maukah kamu jadi bagian dari hari-hariku? 💖",
    "😡 Marah" to "Aku lagi marah banget sekarang. Aku butuh waktu buat tenang, tapi nanti kita bicarakan baik-baik ya.",
    "🥺 Minta maaf" to "Aku minta maaf banget atas kesalahanku. Aku nggak mau kehilangan kamu. Maafin aku ya 🥺",
    "🌙 Rindu" to "Aku kangen kamu. Semoga kamu baik-baik saja di sana 🌙",
    "🙏 Terima kasih" to "Terima kasih ya untuk semuanya. Kamu berarti banget buat aku 🙏"
)

@Composable
fun MainScreen(email: String, sp: android.content.SharedPreferences) {
    val scope = rememberCoroutineScope()
    var target by remember { mutableStateOf("") }
    var text by remember { mutableStateOf(templates[0].second) }
    var last by remember { mutableLongStateOf(sp.getLong("last", 0L)) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var confirm by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    val remain = (COOLDOWN - (now - last)).coerceAtLeast(0)
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.mipmap.ic_launcher), null, Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)))
            Spacer(Modifier.width(10.dp))
            Column { Text("gunzdev", fontWeight = FontWeight.Black, color = Cyan, fontSize = 20.sp)
                Text(email, color = Color.White.copy(.6f), fontSize = 12.sp) }
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Astronaut(120) }
        OutlinedTextField(target, { target = it }, label = { Text("Nomor target (628xxxx)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Text("Pilih jenis pesan", color = Color.White.copy(.8f))
        Row(Modifier.horizontalScroll2(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            templates.forEach { (n, t) -> FilterChip(selected = text == t, onClick = { text = t }, label = { Text(n) }) }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(text, { text = it }, label = { Text("Isi pesan (bisa diedit)") }, modifier = Modifier.fillMaxWidth().heightIn(min = 130.dp))
        Spacer(Modifier.height(14.dp))
        Button(onClick = { confirm = true }, enabled = remain == 0L && !busy && target.length > 6 && text.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
            Text(if (remain > 0) "Cooldown ${remain / 60000}:${"%02d".format(remain / 1000 % 60)}" else "🚀 Kirim Pesan", fontWeight = FontWeight.Bold)
        }
        if (status.isNotEmpty()) Text(status, color = Color.White.copy(.85f), modifier = Modifier.padding(top = 10.dp))
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false },
        title = { Text("Konfirmasi kirim") },
        text = { Text("Kirim pesan ke ${normNum(target)} lewat nomor WhatsApp yang sudah terhubung? Setelah ini ada cooldown 5 menit.") },
        confirmButton = { TextButton(onClick = {
            confirm = false; busy = true; status = "Mengirim..."
            scope.launch {
                val r = withContext(Dispatchers.IO) {
                    try { Api.call("/send", JSONObject().put("id", sid(email)).put("to", normNum(target)).put("text", text)) }
                    catch (e: Exception) { 0 to JSONObject().put("error", "Tidak bisa terhubung ke server") }
                }
                busy = false
                if (r.first == 200) { last = System.currentTimeMillis(); sp.edit().putLong("last", last).apply(); status = "✅ Pesan terkirim!" }
                else if (r.first == 429) { last = System.currentTimeMillis() - (COOLDOWN - r.second.optLong("remainMs")); sp.edit().putLong("last", last).apply(); status = "Masih cooldown." }
                else status = "❌ " + r.second.optString("error", "Gagal (${r.first})")
            }
        }) { Text("Kirim") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Batal") } })
}

@Composable
fun Modifier.horizontalScroll2(): Modifier = this.then(Modifier.horizontalScroll(rememberScrollState()))
