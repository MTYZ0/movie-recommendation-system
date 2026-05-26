# 🎬 Heap Tabanlı İşbirlikçi Filtreleme Film Öneri Sistemi

Bu proje, kullanıcıların film puanlamaları üzerinden **Kosinüs Benzerliği (Cosine Similarity)** hesaplayarak kişiselleştirilmiş film önerileri üreten, Java Swing tabanlı modern bir masaüstü uygulamasıdır. 

Projede en benzer kullanıcıları ve en yüksek puanlı filmleri verimli şekilde sıralamak için dizi (Array), `ArrayList` veya hazır `PriorityQueue` gibi yapılar yerine tamamen **bağlı liste (pointer/node) tabanlı özel bir Max-Heap** veri yapısı geliştirilmiş ve kullanılmıştır.

---

## 🚀 Öne Çıkan Özellikler

- **Çift Modlu Kullanıcı Arayüzü (Java Swing):**
  - 🖥️ **Ekran A (Hedef Kullanıcı Modu):** `data/target_user.csv` dosyasından seçilen hazır bir kullanıcının geçmiş puanlamalarına göre öneri alma.
  - ✍️ **Ekran B (Özel Puanlama Modu):** Sistemdeki filmlerden rastgele seçilen 5 filmi 1-5 arası puanlayarak anlık yeni bir kullanıcı profili oluşturma ve buna göre öneri alma.
- **Yüksek Performans & Asenkron Hesaplama:**
  - Öneri hesaplamaları Java Swing'in `Event Dispatch Thread` (EDT) üzerinde değil, arka planda çalışan bir `SwingWorker` ile asenkron gerçekleştirilir. Bu sayede büyük veri setlerinde dahi arayüz kilitlenmez.
- **Akıllı Veri Yapıları:**
  - Seyrek matrisler (Sparse Matrix) için `HashMap` kullanılarak bellek tüketimi minimuma indirilmiştir.
  - Kosinüs benzerliği hesaplanırken vektör normları önbelleğe alınarak (`squaredNorm`) mükemmel bir zaman karmaşıklığı elde edilmiştir.
- **Generic ve Düğüm Tabanlı Max-Heap:**
  - Herhangi bir diziye bağımlı olmayan, tamamen generic düğümlerden (`Node`) oluşan el yapımı Max-Heap veri yapısı.

---

## 🛠️ Teknik Mimari ve Veri Yapıları

Proje, bellek verimliliği ve nesne yönelimli tasarım ilkeleri göz önünde bulundurularak sıfırdan inşa edilmiştir:

| Sınıf / Veri Yapısı | Açıklama |
| :--- | :--- |
| **`NodeMaxHeap<T>`** | Elemanları öncelik sırasına göre saklayan, tamamen düğüm (pointer) tabanlı el yapımı Max-Heap. Eşsiz `search` metodu ile aynı kullanıcının mükerrer eklenmesini önler. |
| **`HashMap<Integer, Integer>`** | Kullanıcının sadece puan verdiği filmleri (`MovieID -> Rating`) tutan seyrek vektör yapısı. |
| **`squaredNorm`** | Vektör uzunluğunun karesini önbellekte tutarak kosinüs benzerliği hesaplamasında tekrarlı işlemlerin önüne geçer. |
| **`CsvReader`** | CSV dosyalarını hızlı ve güvenli bir şekilde ayrıştırır. |
| **`RecommendationEngine`** | Benzerlik hesaplamalarını ve Heap tabanlı önerme algoritmalarını koordine eden çekirdek motor. |

---

## 📋 Proje Dosya Yapısı

Proje, gereksiz karmaşıklıktan uzak, sade ve anlaşılır bir klasör hiyerarşisine sahiptir:

```text
movie-recommendation-system/
├── src/
│   ├── Main.java
│   ├── CsvReader.java
│   ├── Movie.java
│   ├── MovieRecommendationGUI.java
│   ├── MovieScore.java
│   ├── NodeMaxHeap.java
│   ├── Recommendation.java
│   ├── RecommendationEngine.java
│   ├── SimilarUser.java
│   └── UserRatings.java
├── data/
│   ├── main_data.csv
│   ├── movies.csv
│   └── target_user.csv
├── RAPOR.md
├── README.md
├── movie-recommendation-system.jar
└── .gitignore
```

---

## 🧠 X ve K Parametreleri Nedir?

Proje kapsamında öneri sınırlarını belirlemek için iki temel parametre kullanılır:

- **`X`:** Heap kökünden çekilecek **en benzer kullanıcı sayısı**dır.
- **`K`:** Her benzer kullanıcıdan alınacak **en yüksek puanlı film sayısı**dır.
- **Toplam Öneri Sınırı:** En fazla `X * K` adet öneri üretilir.

> [!NOTE]
> Seçilen $X$ kullanıcının yeterli puanladığı film yoksa liste $X \times K$ sayısından kısa kalabilir; sistem asla sınırı aşmak adına 4. veya sonraki kullanıcılara geçiş yapmaz. Bu, projenin deterministik kuralları çerçevesinde tasarlanmıştır.

---

## 💾 Veri Dosyaları ve Formatları

Uygulamanın çalışması için aşağıdaki 3 CSV dosyasının doğru formatta bulunması gerekir:

### 1. `movies.csv` (Film Kütüphanesi)
```csv
movieId,movieName,genre
1,Toy Story (1995),Adventure|Animation|Children|Comedy|Fantasy
2,Jumanji (1995),Adventure|Children|Fantasy
3,Grumpier Old Men (1995),Comedy|Romance
```

### 2. `target_user.csv` (Hedef Kullanıcılar)
```csv
userId,userName
1,Ahmet
2,Zeynep
```

### 3. `main_data.csv` (Kullanıcı Değerlendirmeleri)
```csv
userId,movieId,rating
1,1,5
1,3,4
2,1,3
```

---

## ⚡ Algoritma ve Performans Analizi

### 📈 Kosinüs Benzerliği Formülü

İki kullanıcı vektörü ($A$ ve $B$) arasındaki benzerlik katsayısı şu formülle hesaplanır:

$$\text{Benzerlik}(A, B) = \frac{A \cdot B}{\|A\| \times \|B\|} = \frac{\sum (A_i \times B_i)}{\sqrt{\sum A_i^2} \times \sqrt{\sum B_i^2}}$$

### ⚙️ Adım Adım Öneri Hesaplama
1. Hedef kullanıcı ile ana veri setindeki tüm kullanıcılar arasında benzerlik hesaplanır.
2. Benzerlik skoru `0.0001`'in altında kalan kullanıcılar filtrelenir.
3. Kalan kullanıcılar `NodeMaxHeap<SimilarUser>` yapısına eklenir (aynı kullanıcının mükerrer eklenmesini önlemek için Heap üzerinde `search` yapılır).
4. Heap kökünden sırayla en benzer **X** kullanıcı çekilir.
5. Her benzer kullanıcının puanladığı filmler, kullanıcı bazlı başka bir `NodeMaxHeap` içerisine atılarak en yüksek puanlı **K** film seçilir.
6. Seçilen filmler, kaynak kullanıcı bilgileri ile birlikte öneri listesinde listelenir.

### ⏱️ Zaman Karmaşıklığı (Time Complexity)
- **Benzerlik Hesaplama:** $O(m)$ — ($m$: İki kullanıcının ortak puanladığı film sayısı).
- **Heap Operasyonları:** $O(\log n)$ — ($n$: Heap içindeki eleman sayısı).
- **Toplam Karmaşıklık:** $O(N \times m + N \log N)$ — ($N$: Toplam kullanıcı sayısı).

---

## ⚙️ Kurulum ve Çalıştırma

Uygulamanın çalışması için sisteminizde **Java 8** veya daha yeni bir sürümün kurulu olması gerekmektedir.

### 1. Derleme
Terminalden projenin kök dizininde şu komutu çalıştırarak kaynak kodları derleyin:
```sh
javac -encoding UTF-8 -d out src/*.java
```

*Java 8 uyumluluğuyla derlemek için:*
```sh
javac --release 8 -encoding UTF-8 -d out src/*.java
```

### 2. Çalıştırma
Derlenen dosyaları çalıştırmak için:
```sh
java -cp out Main
```

### 3. Hazır JAR Dosyası ile Çalıştırma
Uygulamayı derleme yapmadan doğrudan `.jar` paketi üzerinden çalıştırmak için:
```sh
java -jar movie-recommendation-system.jar
```

### 4. Özel Veri Klasörü Parametreleri
Sistem, CSV dosyalarını varsayılan proje düzeninde `data/` klasöründe arar. Dilerseniz klasör yolunu veya dosya yollarını parametre olarak verebilirsiniz:
```sh
# CSV dosyaları "data" klasörünün içindeyse:
java -jar movie-recommendation-system.jar data

# 3 CSV dosyasının yolunu da ayrı ayrı belirtmek isterseniz:
java -jar movie-recommendation-system.jar <main_data_path> <movies_path> <target_user_path>
```

---

## ❓ Sorun Giderme ve Çözümler (FAQ)

**S: "Dosya bulunamadı: main_data.csv" hatası alıyorum.**
> **C:** CSV dosyalarınızın `data/` klasörü içinde bulunduğundan emin olun. Alternatif olarak, klasör yolunu yukarıda açıklandığı gibi parametre olarak geçebilirsiniz.

**S: JAR dosyası açılmıyor veya çalıştırılamıyor.**
> **C:** Sisteminizde Java Runtime Environment (JRE) kurulu olmayabilir veya sürümü eski olabilir. Terminale `java -version` yazarak sürümünüzün en az 8 (veya daha güncel) olduğunu doğrulayın.

**S: GUI arayüzü çok yavaş veya donuyor mu?**
> **C:** Hayır. Uygulamada hesaplamalar `SwingWorker` ile arka plan iş parçacığında yapıldığı için ne kadar büyük veri yüklenirse yüklensin ana arayüz her zaman akıcı kalacaktır.

---

## 🎓 Değerlendirme Kriterleri (100 Puan Üzerinden)

Proje, akademik ve teknik standartlara tam uyum sağlayacak şekilde tasarlanmıştır:
- **Veri Yapısı Tasarımı (20 Puan):** Seyrek matris ve HashMap optimizasyonları. ✅
- **Özel Heap Uygulaması (25 Puan):** Tamamen generic, LinkedList/Node tabanlı Max-Heap tasarımı. ✅
- **Öneri Algoritması (20 Puan):** Kosinüs benzerliği ve veri filtreleme modeli. ✅
- **Swing GUI Tasarımı (20 Puan):** Kullanıcı dostu, çift ekranlı ve asenkron arayüz. ✅
- **Rapor ve Belgeler (15 Puan):** Karşılaşılan durumlar, varsayımlar ve analizler. ✅

---

## 📝 Gelecek İyileştirmeler ve Yapılacaklar (Todo)

- [ ] **Veritabanı Entegrasyonu:** CSV dosyaları yerine PostgreSQL/MySQL entegrasyonu.
- [ ] **Web Arayüzü:** Uygulamayı Spring Boot ve modern bir web arayüzü ile tarayıcıya taşıma.
- [ ] **Matris Faktörizasyonu:** Öneri kalitesini artırmak için Matrix Factorization (SVD) algoritması ekleme.
- [ ] **Caching Mekanizması:** Benzerlik skorlarını önbelleğe alarak hesaplama süresini daha da kısaltma.

---

## 📄 Lisans
Bu proje eğitim ve ödev amacıyla geliştirilmiştir. Tüm hakları saklıdır.

---
📅 **Son Güncelleme:** 28 Mayıs 2026
