/**
 * Dizi kullanmadan yazılmış node tabanlı Max-Heap veri yapısıdır.
 *
 * Klasik heap'ler çoğunlukla dizi ile temsil edilir. Bu projedeki kritik
 * kısıt nedeniyle heap fiziksel olarak ağaç düğümleriyle tutulur. Ağacın tam
 * ikili ağaç düzeni korunur; yeni düğümün veya son düğümün konumu, düğüm
 * sayısının ikili gösterimi üzerinden bulunur.
 *
 * @param <T> Heap içinde tutulacak karşılaştırılabilir veri tipi.
 */
public class NodeMaxHeap<T extends Comparable<T>> {
    private HeapNode<T> root;
    private int size;

    public void insert(T value) {
        HeapNode<T> newNode = new HeapNode<T>(value);

        if (root == null) {
            root = newNode;
            size = 1;
            return;
        }

        int newPosition = size + 1;
        HeapNode<T> parent = findParentNodeForPosition(newPosition);
        newNode.parent = parent;

        if (isLeftChildPosition(newPosition)) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        size++;
        bubbleUp(newNode);
    }

    public T peek() {
        return root == null ? null : root.value;
    }

    /**
     * Heap kökündeki en büyük elemanı döndürür ve ağaçtan çıkarır.
     */
    public T extractMax() {
        if (root == null) {
            return null;
        }

        T maxValue = root.value;

        if (size == 1) {
            root = null;
            size = 0;
            return maxValue;
        }

        HeapNode<T> lastNode = findNodeAtPosition(size);
        root.value = lastNode.value;
        detachLastNode(lastNode);
        size--;
        heapifyDown(root);

        return maxValue;
    }

    /**
     * Heap ağacı üzerinde dizi kullanmadan arama yapar.
     *
     * Heap sıralama avantajı doğrudan "en büyük elemanı bulma" işleminde
     * kullanılır; genel koşullu arama doğası gereği O(N) sürebilir. Bu metot,
     * ödevdeki "arama işlemi heap ile yapılmalıdır" kısıtını node tabanlı
     * yapı içinde karşılamak için sağlanır.
     */
    public T search(SearchCondition<T> condition) {
        return search(root, condition);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    private T search(HeapNode<T> currentNode, SearchCondition<T> condition) {
        if (currentNode == null) {
            return null;
        }

        if (condition.matches(currentNode.value)) {
            return currentNode.value;
        }

        T leftResult = search(currentNode.left, condition);

        if (leftResult != null) {
            return leftResult;
        }

        return search(currentNode.right, condition);
    }

    /**
     * Yeni eklenecek pozisyonun ebeveynini bulur.
     *
     * Örnek: Pozisyon 13 ikilikte 1101'dir. İlk 1 kökü temsil eder.
     * Son bit yeni düğümün ebeveynin hangi tarafına bağlanacağını gösterir.
     * Aradaki bitler kökten ebeveyne giden yolu oluşturur.
     */
    private HeapNode<T> findParentNodeForPosition(int position) {
        String binaryPath = Integer.toBinaryString(position);
        HeapNode<T> currentNode = root;

        for (int index = 1; index < binaryPath.length() - 1; index++) {
            currentNode = binaryPath.charAt(index) == '0'
                    ? currentNode.left
                    : currentNode.right;
        }

        return currentNode;
    }

    private HeapNode<T> findNodeAtPosition(int position) {
        String binaryPath = Integer.toBinaryString(position);
        HeapNode<T> currentNode = root;

        for (int index = 1; index < binaryPath.length(); index++) {
            currentNode = binaryPath.charAt(index) == '0'
                    ? currentNode.left
                    : currentNode.right;
        }

        return currentNode;
    }

    private boolean isLeftChildPosition(int position) {
        return position % 2 == 0;
    }

    private void detachLastNode(HeapNode<T> lastNode) {
        HeapNode<T> parent = lastNode.parent;

        if (parent.left == lastNode) {
            parent.left = null;
        } else {
            parent.right = null;
        }
    }

    private void bubbleUp(HeapNode<T> node) {
        HeapNode<T> currentNode = node;

        while (currentNode.parent != null
                && currentNode.value.compareTo(currentNode.parent.value) > 0) {
            swapValues(currentNode, currentNode.parent);
            currentNode = currentNode.parent;
        }
    }

    private void heapifyDown(HeapNode<T> node) {
        HeapNode<T> currentNode = node;

        while (currentNode != null) {
            HeapNode<T> largestNode = currentNode;

            if (currentNode.left != null
                    && currentNode.left.value.compareTo(largestNode.value) > 0) {
                largestNode = currentNode.left;
            }

            if (currentNode.right != null
                    && currentNode.right.value.compareTo(largestNode.value) > 0) {
                largestNode = currentNode.right;
            }

            if (largestNode == currentNode) {
                break;
            }

            swapValues(currentNode, largestNode);
            currentNode = largestNode;
        }
    }

    private void swapValues(HeapNode<T> firstNode, HeapNode<T> secondNode) {
        T temporaryValue = firstNode.value;
        firstNode.value = secondNode.value;
        secondNode.value = temporaryValue;
    }

    /**
     * Heap'in iç düğüm sınıfıdır. Dışarı açılmaz; heap bütünlüğünü sadece
     * NodeMaxHeap sınıfı yönetir.
     */
    private static class HeapNode<E> {
        private E value;
        private HeapNode<E> parent;
        private HeapNode<E> left;
        private HeapNode<E> right;

        HeapNode(E value) {
            this.value = value;
        }
    }

    public interface SearchCondition<E> {
        boolean matches(E value);
    }
}
