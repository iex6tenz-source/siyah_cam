# Siyah Cam Dosya Yöneticisi

Kotlin + Jetpack Compose ile hazırlanmış başlangıç Android Studio projesi.

## Özellikler
- Koyu sıvı cam görünümlü Türkçe arayüz
- Android Sistem Dosya Seçici üzerinden kullanıcının seçtiği klasöre erişim
- Klasör içindeki dosyaları listeleme
- Seçilen klasör ağacında dosya boyutu analizi
- Fotoğraf/video/belge sayımları
- Gizli, `.tmp`, `.temp`, `.log`, `.bak` dosyalarını temizlik adayı olarak gösterme
- Silme öncesi onay ekranı
- 100 MB üzerindeki büyük dosyaları gösterme

## Derleme
1. Android Studio (Giraffe veya daha yeni) kur.
2. Bu klasörü Android Studio'da aç.
3. Gradle senkronizasyonunu tamamla.
4. Telefonunda Geliştirici seçenekleri ve USB hata ayıklamayı aç veya emülatör kullan.
5. Run düğmesine bas.

## Önemli sınırlar
- Android'in sistem ve diğer uygulamaların özel önbelleklerini sessizce temizlemesine izin verilmez. Bu proje yalnızca kullanıcının sistem dosya seçicisiyle açıkça verdiği klasör erişimi kapsamında çalışır.
- Temizlik adayları yalnızca dosya adına/uzantısına göre tahmin edilir; hepsinin gereksiz olduğu garanti edilmez. Silmeden önce listeyi kontrol edin.
- Bazı sağlayıcılar veya Android sürümleri, seçilen klasör içindeki bazı öğelerin silinmesine izin vermeyebilir.
- Bu bir Android Studio kaynak projesidir; bu ortamda imzalı APK derlenmiş değildir.
