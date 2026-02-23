# Lightning Tracker

Gerçek zamanlı yıldırım ve şimşek aktivitelerini harita üzerinde anlık olarak takip etmenizi sağlayan, Kotlin ile geliştirilmiş bir Android uygulaması.

## 🚀 Özellikler

* **Gerçek Zamanlı Veri Akışı:** WebSocket bağlantısı sayesinde doğa olaylarını (yıldırım/şimşek) saniyesinde yakalayın ve izleyin.
* **Harita Entegrasyonu:** Google Maps üzerinde olayların gerçekleştiği konumları dinamik pinler ile görselleştirin.
* **Detaylı Liste Görünümü:** Tespit edilen hava olaylarının loglarını veya detaylarını performanslı bir liste akışında inceleyin.
* **Kesintisiz Deneyim:** Arka planda çalışan asenkron işlemler sayesinde uygulama arayüzünde donma olmadan akıcı kullanım.

## 🛠️ Kullanılan Teknolojiler ve Mimari

Bu proje, modern Android geliştirme standartlarına uygun olarak inşa edilmiştir:

* **Dil:** Kotlin
* **Harita Servisi:** Google Maps API
* * **Hava Durumu Servisi:** WeatherAPI
* **Ağ Bağlantısı & WebSocket:** OkHttpClient
* **Asenkron İşlemler:** Coroutines
* **Arayüz Bileşenleri:** RecyclerView, ViewBinding / DataBinding
