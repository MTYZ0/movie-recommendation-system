# Yığın Tabanlı İşbirlikçi Filtreleme Film Öneri Sistemi

## Projenin Amacı

Bu proje, kullanıcıların film puanları üzerinden kosinüs benzerliği hesaplayarak
film önerisi üretir. En benzer kullanıcıları sıralamak için dizi kullanmayan,
node tabanlı özel bir Max-Heap veri yapısı kullanılmıştır.

## X ve K Parametre Tanımı

PDF metninde X ve K parametreleri farklı bölümlerde farklı sırayla
anlatılabildiği için bu projede arayüz gereksinimi esas alınmıştır.
Kod, arayüz ve rapor boyunca X ve K için yalnızca aşağıdaki anlamlar
kullanılmıştır:

- `X`: Heap kökünden çekilecek en benzer kullanıcı sayısıdır.
- `K`: Her benzer kullanıcıdan alınacak en yüksek puanlı film sayısıdır.
- Toplam üst sınır `X * K` öneridir.

Örneğin `X = 3` ve `K = 3` girilirse algoritma en fazla 3 benzer kullanıcıyı
seçer ve bu kullanıcıların her birinden en fazla 3 film alır. Böylece sonuç
listesi en fazla 9 satır olur. Seçilen X kullanıcının yeterli filmi yoksa liste
9'dan kısa kalabilir; algoritma X sınırını aşmak için 4. kullanıcıya geçmez.

## Kullanılan Veri Yapıları

- `HashMap<Integer, Integer>`: Kullanıcı-film puanlarını seyrek vektör olarak
  tutar. CSV matrisi çok geniş ve çoğunlukla 0 değerli olduğu için bu seçim
  bellek kullanımını azaltır.
- `UserRatings.squaredNorm`: Kosinüs benzerliğinde kullanılan vektör normu için
  kareler toplamını önbellekte tutar. Böylece her kullanıcı karşılaştırmasında
  norm tekrar tekrar hesaplanmaz.
- `NodeMaxHeap<T>`: Benzer kullanıcıları ve kullanıcı bazlı en yüksek puanlı
  filmleri sıralamak için kullanılır. Heap içinde `Array`, `ArrayList` veya
  `PriorityQueue` kullanılmamıştır. En iyi eleman araması heap kökü üzerinden
  `peek` ve `extractMax` işlemleriyle yapılır. Ek olarak, ödevdeki arama
  kısıtı için node tabanlı `search` metodu sağlanmıştır ve benzer kullanıcı
  heap'ine aynı user_id değerinin ikinci kez eklenmesini önlemek için aktif
  olarak kullanılır.
- `List<T>`: CSV'den okunan kullanıcı ve film koleksiyonlarını arayüzde ve
  servis katmanında taşımak için kullanılır.
- `Set<Integer>`: Rastgele film seçimi sırasında aynı indeksin iki kez
  seçilmesini engeller.

## Algoritma

1. `main_data.csv`, `target_user.csv` ve `movies.csv` dosyaları `CsvReader`
   sınıfı tarafından okunur.
2. Her kullanıcı için sadece 0'dan büyük puanlar saklanır.
3. Hedef kullanıcı ile ana veri setindeki her kullanıcı arasında kosinüs
   benzerliği hesaplanır.
4. Benzerliği `0.0001` eşiğinin altında kalan kullanıcılar öneri kalitesini
   düşürmemesi için elenir.
5. Eşik üstü benzerlikli kullanıcılar `NodeMaxHeap<SimilarUser>` içine eklenir.
   Eklemeden önce heap üzerindeki `search` ile aynı `user_id` daha önce eklenmiş
   mi diye kontrol edilir.
6. Bu projede GUI tanımı esas alınmıştır: X benzer kullanıcı sayısı, K ise
   kullanıcı başına seçilecek film sayısıdır.
7. Heap kökünden en fazla X adet en benzer kullanıcı çekilir ve her kullanıcı
   için en yüksek puanlı K film yine node tabanlı heap ile seçilir.
8. Hedef kullanıcının daha önce puanladığı filmler ve farklı benzer
   kullanıcılardan gelen aynı filmler ayrıca elenmez; böylece ödev metnindeki
   "her benzer kullanıcının en yüksek puanlı K filmi" yorumu korunur.
9. Eğer seçilen X kullanıcının puanladığı yeterli film yoksa liste X*K
   değerinden kısa kalabilir; X kullanıcı sınırı yine de aşılmaz.
10. Arayüzde film ID yerine film adı gösterilir.

## Varsayımlar ve Yorum Farkları

- X ve K parametreleri bu raporun "X ve K Parametre Tanımı" bölümündeki
  anlamlarıyla kullanılmıştır.
- Hedef kullanıcının daha önce puanladığı filmler öneri listesinden çıkarılmaz.
  Gerçek sistemlerde bu filtre çoğu zaman eklenir; ancak bu projede PDF'in
  "benzer kullanıcıların en yüksek puanlı filmlerini listele" ifadesiyle
  çakışmamak için uygulanmamıştır.
- Aynı film farklı benzer kullanıcılar tarafından yüksek puanlanmışsa sonuç
  listesinde birden fazla kez görünebilir. Bu bilinçli olarak korunmuştur;
  çünkü ödev metni "her benzer kullanıcıdan K film" çıktısını işaret eder.
  Listede kaynak kullanıcı bilgisi gösterildiği için tekrarların hangi benzer
  kullanıcıdan geldiği izlenebilir.
- İlk X kullanıcıdan yeterli film çıkmazsa liste X*K değerinden kısa kalabilir.
  Bu durumda algoritma X kullanıcı sınırını aşmaz; bulunan önerileri gösterir.
- Benzerliği `0.0001` altında olan kullanıcılar alınmaz. Ortak puanlanan film
  yoksa kosinüs benzerliği 0 olacağı için öneri bulunamayabilir.
- Ekran B'de "5 ComboBox, her ComboBox'ta 10 rastgele film" gereksinimi her
  ComboBox'a ayrı 10 film atanarak yorumlanmıştır. Yani tüm ComboBox'lar aynı
  ortak 10 filmi paylaşmaz; toplamda 50 rastgele ve benzersiz seçenek 5 kutuya
  10'ar film olarak dağıtılır. Bu tercih, kullanıcının yanlışlıkla aynı filmi
  iki farklı satırda seçme ihtimalini azaltmak için yapılmıştır. Eğer veri
  setinde 50'den az film varsa seçenekler kutulara döngüsel dağıtılır; böylece
  en az bir film bulunduğu sürece hiçbir ComboBox boş kalmaz. Aynı film farklı
  kutularda seçilirse arayüz doğrulaması kullanıcıyı uyarır.

## Kosinüs Benzerliği

Kullanıcı vektörleri A ve B için:

```text
similarity(A, B) = dot(A, B) / (||A|| * ||B||)
```

Vektörlerden biri boşsa veya ortak puan bilgisi yoksa benzerlik 0 kabul edilir.
Benzerlik skorları heap'e eklenmeden önce 5 ondalık basamağa yuvarlanır.

## Arayüz

Uygulama Java Swing ile geliştirilmiştir.

- Ekran A: `target_user.csv` içindeki hedef kullanıcılar ComboBox ile seçilir.
  X benzer kullanıcı ve K film/kullanıcı parametreleri girilerek öneri alınır.
- Ekran B: 5 adet ComboBox içinde her biri 10 farklı rastgele film gösterilir.
  Kullanıcı 5 filmi 1-5 arası puanlar, bu puanlar hedef vektöre dönüştürülür
  ve öneri üretilir.
- Film puanlama ekranı 5 satır ve 5 görsel hücreden oluşan bir düzen kullanır:
  film etiketi, film ComboBox'ı, puan etiketi, puan alanı ve `[1-5]` aralık
  etiketi.
- Öneri hesaplamaları Swing `Event Dispatch Thread` üzerinde yapılmaz;
  `SwingWorker` ile arka planda çalıştırılır. Böylece büyük veri setlerinde
  arayüzün donması engellenir.
- Rastgele film listeleri için tüm film koleksiyonu kopyalanıp karıştırılmaz;
  benzersiz rastgele indeksler seçilerek sadece ihtiyaç duyulan film sayısı
  kadar eleman alınır.

## Çalıştırma

Proje Java 8 uyumlu sözdizimiyle derlenebilir.

Derleme:

```sh
javac -encoding UTF-8 -d out src/*.java
```

Java 8 hedefli derleme:

```sh
javac --release 8 -encoding UTF-8 -d out src/*.java
```

Çalıştırma:

```sh
java -cp out Main
```

JAR çalıştırma:

```sh
java -jar movie-recommendation-system.jar
```

CSV dosyaları çalışma klasöründe, JAR ile aynı klasörde veya `data/` klasörü
içinde bulunabilir. Alternatif olarak tek argümanla veri klasörü verilebilir:

Teslim ZIP'i içinde `movie-recommendation-system.jar`, `main_data.csv`,
`movies.csv` ve `target_user.csv` aynı klasör seviyesinde tutulmuştur. CSV
dosyaları JAR içine gömülmemiştir; program bu dosyaları dış dosya olarak
bulacak şekilde tasarlanmıştır.

```sh
java -jar movie-recommendation-system.jar data
```

Üç argüman verilirse sırasıyla `main_data.csv`, `movies.csv`,
`target_user.csv` yolları kabul edilir.
