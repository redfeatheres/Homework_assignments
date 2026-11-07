public class Main {
    public static void main(String[] args) {
        MyHashMap<Integer, Integer> map = new MyHashMap<>();
        System.out.println(map.size());
        for (int i = 1; i <= 100; i++) {
            map.put(i, i * 2);
        }
        System.out.println(map.size());
        System.out.println("get: " + map.get(3));
        map.remove(5);
        map.remove(5);
        System.out.println(map.size());

        MyHashMap<String, Integer> map2 = new MyHashMap<>();
        System.out.println(map2.size());
        for (int i = 1; i <= 100; i++) {
            map2.put("key-" + i, i);
        }
        System.out.println(map2.size());
        System.out.println("get: " + map2.get("key-10"));
        map2.remove("key-10");
        System.out.println(map2.size());
        System.out.println("get: " + map2.get("key-10"));
    }
}

