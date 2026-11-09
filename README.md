# HyprDroid

HyprDroid 1.5.0, Hyprland’den esinlenen gerçek bir Android **HOME launcher** uygulamasıdır. Ana ekranda saat kartı kaldırıldı; terminal prompt’u onun yerini aldı. Uygulama çekmecesindeki başlık da aynı çalışan komut satırıyla değiştirildi. Waybar benzeri şeffaf sistem barı ağ türünü, pil yüzdesini, RAM’i ve örneklenen CPU yükünü gösterir. İlk açılış kişiselleştirmesi, workspace’ler, Android widget’ları, dock, uygulama komutları ve Termux Debian komut köprüsü bulunur. Android 12+’ta wallpaper arka planına blur uygulanır; daha eski sürümlerde yarı saydam paneller desteklenir. Kotlin, Jetpack Compose ve Material 3 ile hazırlanmıştır. Android’in pencere yöneticisini değiştirmez.

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

### İmzalı release APK

Kendi keystore dosyanızı proje dışında saklayın. `keystore.properties.example` dosyasını `keystore.properties` olarak kopyalayıp keystore yolunu ve parolalarını girin; `keystore.properties` ile `.jks`/`.keystore` dosyaları Git tarafından yok sayılır. Ardından `./gradlew assembleRelease` çalıştırın. Release APK `app/build/outputs/apk/release/app-release.apk` konumunda oluşur. Aynı uygulamanın ilerideki sürümlerini güncelleyebilmek için keystore’un güvenli bir yedeğini alın ve kaybetmeyin.

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

Yeni kurulumda ilk açılış sihirbazı vurgu rengini, panel saydamlığını, galeriden duvar kâğıdını, dock uygulama adlarını ve geçiş animasyonlarını seçtirir. Tercihler cihazda saklanır; sihirbaz bir kez görünür, seçimler daha sonra Ayarlar’dan değiştirilebilir. Önceki HyprDroid sürümünden yükselten kullanıcılar bu ilk-kurulum ekranını tekrar görmez.

Saat kartının bulunduğu ana ekran prompt’una veya uygulama çekmecesinin üst prompt’una `run Ayarlar` ya da `run instagra` yazıp klavyedeki Git/Enter düğmesine dokunun. Uygulama adının tamamı veya tek uygulamaya eşleşen başlangıç kısmı çalışır; paket kodu da kabul edilir: `pkg:com.whatsapp` veya `com.android.settings`. Belirsiz eşleşmelerde rastgele uygulama açmak yerine tam ad ister. Yukarı kaydırma çekmeceyi açar; çekmecedeki ayrı arama alanı uygulama listesini anlık filtreler.

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

Ayarlar ekranında altı vurgu renginden birini, galeriden duvar kâğıdını, panellerin saydamlığını, dock kapasitesini ve dock uygulama adlarını seçebilirsiniz. “Freeform pencereler” anahtarı açıksa komutla, çekmeceden, workspace kısayolundan ve dock’tan başlatılan uygulamalar için Android’e ekran koordinatlarında bir `ActivityOptions.setLaunchBounds` isteği gönderilir; kapalıysa standart başlatma kullanılır. Yumuşak geçiş animasyonları da açılıp kapatılabilir. “Varsayılanlara dön” launcher tercihlerini, çalışma alanı kısayollarını ve eklenmiş widget’ları sıfırlar.

### Freeform gereksinimleri ve sınırları

`setLaunchBounds` gerçek bir pencere garantisi vermez. Android, cihaz freeform window-management özelliğini bildirmiyorsa bu bounds isteğini yok sayabilir; üretici arayüzü veya başlatılan uygulama da pencere düzenini değiştirebilir. Launcher’daki durum satırı bu nedenle “istek gönderildi” der, pencerenin gerçekten yüzen moda geçtiğini iddia etmez. Android’in [ActivityOptions API belgesi](https://developer.android.com/reference/android/app/ActivityOptions) ve [çoklu pencere cihaz gereksinimleri](https://source.android.com/docs/core/display/multi-window) cihaz/ROM desteğinin belirleyici olduğunu açıklar. Bu özellik Galaxy A25 üzerinde henüz doğrulanmamıştır.

Root erişimli bir cihazda freeform denemek isterseniz, aşağıdaki ayarları **kendi root yetkili terminalinizde** çalıştırın; HyprDroid bunları otomatik çalıştırmaz:

```sh
su -c 'settings put global enable_freeform_support 1'
su -c 'settings put global force_resizable_activities 1'
```

İkinci ayar bazı uygulamaların normal pencere boyutlandırma varsayımlarını değiştirebilir. Sorun yaşarsanız aynı anahtarları `0` yaparak kapatın. Bu cihaz ayarları launcher tercihlerinden ayrıdır; root erişimi veya ROM desteği yoksa komutlar çalışmayabilir. Anahtarların AOSP’deki karşılıkları [`Settings.java`](https://android.googlesource.com/platform/frameworks/base/+/android16-release/core/java/android/provider/Settings.java) kaynağında yer alır.

### `hyprdroid.conf` ve Termux’tan düzenleme

Ayarlar → **HYPRDROID.CONF** → **Config oluştur** ile dosyayı Android belge seçicisinde `Documents/HyprDroid/hyprdroid.conf` konumuna kaydedin. Ayarları Termux’tan düzenlemek için Termux’ta paylaşılan depolama iznini verip config’i `~/.config/hypr/` altına eşleyin:

```sh
termux-setup-storage
mkdir -p ~/.config/hypr
ln -sf "$HOME/storage/shared/Documents/HyprDroid/hyprdroid.conf" ~/.config/hypr/hyprdroid.conf
nano ~/.config/hypr/hyprdroid.conf
```

Belgeler seçicisinde farklı bir konum seçtiyseniz symlink hedefini ona göre değiştirin. Düzenledikten sonra HyprDroid → Ayarlar → **Termux düzenlemesinden sonra yenile** düğmesine dokunun. Dosya, uygulama yeniden açıldığında da otomatik okunur. Düzenlenebilir başlangıç örneği [`hyprdroid.conf.example`](hyprdroid.conf.example) dosyasında bulunur.

Bu, **HyprDroid’e özel küçük bir Hyprland-esintili alt kümedir; gerçek Hyprland config’i değildir**. `general` bölümünde vurgu (`lime`, `cyan`, `pink`, `purple`, `amber`, `mint`), saydamlık, `gap` (dp), `border` (dp), dock sayısı/etiketleri, animasyon ve freeform tercihi; `bind` bölümünde yalnızca izin verilen gesture eylemleri; `workspace` bölümünde üç ad ve uygulama kuralları okunur. Örneğin `rule.com.instagram.android = 2`, Instagram’ı başlatırken launcher’ın seçili workspace’ini 2 yapar; Android uygulama penceresini o workspace’e taşımaz. Tanınmayan anahtarlar uygulanmaz, sayısal değerler güvenli aralıklarla sınırlanır. HyprDroid `bind` üzerinden shell komutu çalıştırmaz.

## Teknik notlar

Manifestte `MAIN` + `HOME` + `DEFAULT` intent filtresi bulunur. Uygulama keşfi, `<queries>` altında yalnızca `MAIN` + `LAUNCHER` etkinlikleriyle sınırlandırılmıştır; geniş `QUERY_ALL_PACKAGES` izni kullanılmaz. Linux komut köprüsü Termux’un resmi `RUN_COMMAND` servisini ve kullanıcı tarafından verilecek `com.termux.permission.RUN_COMMAND` iznini kullanır.

HyprDroid gerçek bir Android launcher’dır fakat Android içinde root yetkisi sağlamaz, Linux/Wayland pencere yöneticisini çalıştırmaz ve Android pencere yöneticisini kontrol etmez. Freeform seçeneği yalnızca uygulama başlatılırken pencere bounds’u talep eder; gerçek pencereleme/tiling davranışı cihaz, ROM ve hedef uygulamaya bağlıdır. Debian komutları Termux’un PRoot kullanıcı alanında çalışır; gerçek root, kernel modülleri ve Android sistemini değiştirme yetkisi vermez. Debug APK, bu build ortamının debug anahtarıyla imzalanır; kişisel release keystore’u olmadan production-signed/Play Store paketi değildir. Varsayılan ana ekran olarak seçilmesi kullanıcı onayı gerektirir. Görünür uygulama ve kullanılabilir widget sayısı cihaza, üreticiye ve yüklü uygulamalara bağlıdır.
