# HyprDroid

HyprDroid 1.2, Hyprland’den esinlenen gerçek bir Android **HOME launcher** uygulamasıdır. Ana ekran; translucent/blur’lu Waybar benzeri üst paneli, seçilebilir workspace pills, renkli pencere kontrol noktaları, neon kenarlı floating window kartları ve cam görünümlü dock’u tek bir Hyprland esintili görsel dile taşır. Android 12+’ta wallpaper arka planına blur uygulanır; daha eski sürümlerde yarı saydam paneller desteklenir. Kotlin, Jetpack Compose ve Material 3 ile hazırlanmıştır. Android’in pencere yöneticisini değiştirmez.

## Gereksinimler

- Android Studio (güncel kararlı sürüm)
- JDK 17 veya üstü
- Android SDK Platform 36
- Gradle Wrapper 8.13
- Minimum Android sürümü: Android 8.0 / API 26
- Derleme hedefi: Android 16 / API 36

## Derleme

Projeyi Android Studio’da açıp Gradle senkronizasyonunu tamamlayın. Terminalden debug APK üretmek için:

```bash
./gradlew assembleDebug
```

APK şu konumda üretilir: `app/build/outputs/apk/debug/app-debug.apk`.

JVM testleri ve Android lint kontrolleri:

```bash
./gradlew testDebugUnitTest lintDebug
```

APK’yı bağlı bir Android cihaza yüklemek için:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Kurulumdan sonra Android’in “Ana ekran uygulaması” seçicisinde **HyprDroid**’u varsayılan olarak seçin. Seçici görünmezse Android Ayarları → Uygulamalar → Varsayılan uygulamalar → Ana ekran uygulaması yolunu kullanın.

## Kullanım

Ana ekranda komut alanına `run Ayarlar` veya `run instagra` yazıp klavyedeki Git/Enter düğmesine dokunarak, uygulama adının tamamı ya da tek bir uygulamaya eşleşen başlangıç kısmıyla uygulama açabilirsiniz. Paket kodu da kabul edilir: `pkg:com.whatsapp` veya `com.android.settings`. Komut, Android’in kurulu uygulama listesindeki ad/paket eşleşmesini bulur ve PackageManager üzerinden başlatır; belirsiz eşleşmelerde rastgele uygulama seçmek yerine tam ad ister. Yukarı kaydırma uygulama çekmecesini açar. Çekmecedeki arama anlık filtreler ve liste alfabetik sıralanır; bir uygulama simgesine dokunmak da uygulamayı başlatır.

Linux komutları HyprDroid içinde Android sistemine değil, Termux’taki Debian PRoot ortamına yönlendirilir. Termux’u F-Droid’den veya resmi GitHub sürümünden kurup ilk kez açtıktan sonra Termux’ta çalıştırın:

```sh
pkg update
pkg install proot-distro
proot-distro install debian
mkdir -p ~/.termux
nano ~/.termux/termux.properties
```

`termux.properties` dosyasına `allow-external-apps=true` satırını ekleyip kaydedin ve Termux’ta `termux-reload-settings` çalıştırın. Android Ayarları → Uygulamalar → HyprDroid → İzinler bölümünde “Termux ortamında komut çalıştırma” iznini verin. Sonra HyprDroid komut alanına `ls`, `uname -a` veya `apt update` yazabilirsiniz; `term` etkileşimli Debian kabuğunu açar. Kurulum ve sorun giderme ayrıntıları için [Termux entegrasyon notlarına](TERMUX-INTEGRATION.md) bakın.

Ana ekranda boş alana uzun basarak düzenleme kipine girin. Buradan uygulama kısayolları ve Android widget’ları ekleyebilirsiniz. Uygulama çekmecesinde bir uygulamaya uzun basıp hedef çalışma alanını seçerek kısayol eklenir. Her workspace için kısayollar ve AppWidgetHost kimlikleri ayrı saklanır. Widget seçimi sistemin widget picker’ını, çizimi ise Android AppWidgetHost API’sini kullanır.

Ayarlar ekranında altı vurgu renginden birini, galeriden duvar kâğıdını, panellerin saydamlığını, dock kapasitesini ve dock uygulama adlarını seçebilirsiniz. Yumuşak geçiş animasyonları açılıp kapatılabilir. “Varsayılanlara dön” launcher tercihlerini, çalışma alanı kısayollarını ve eklenmiş widget’ları sıfırlar.

## Teknik notlar

Manifestte `MAIN` + `HOME` + `DEFAULT` intent filtresi bulunur. Uygulama keşfi, `<queries>` altında yalnızca `MAIN` + `LAUNCHER` etkinlikleriyle sınırlandırılmıştır; geniş `QUERY_ALL_PACKAGES` izni kullanılmaz. Linux komut köprüsü Termux’un resmi `RUN_COMMAND` servisini ve kullanıcı tarafından verilecek `com.termux.permission.RUN_COMMAND` iznini kullanır.

HyprDroid gerçek bir Android launcher’dır fakat Android içinde root yetkisi sağlamaz, Linux/Wayland pencere yöneticisini çalıştırmaz, Android pencere yöneticisini kontrol etmez ve uygulama pencerelerini Hyprland gibi düzenlemez. Debian komutları Termux’un PRoot kullanıcı alanında çalışır; gerçek root, kernel modülleri ve Android sistemini değiştirme yetkisi vermez. Varsayılan ana ekran olarak seçilmesi kullanıcı onayı gerektirir. Görünür uygulama ve kullanılabilir widget sayısı cihaza, üreticiye ve yüklü uygulamalara bağlıdır.
