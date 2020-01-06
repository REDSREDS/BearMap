import java.util.*;

public class Autocomplete {

//    declare a root TrieNode for autocomplete
    private TrieNode root;
    private LinkedHashMap<Long, GraphDB.Node> nodes;

    /**
     * create TrieNode class
     */
    static class TrieNode {
        TrieNode[] children = new TrieNode[27];
        boolean end;
        String value;
        LinkedList<Long> ids;

        TrieNode() {
            end = false;
            for(int i = 0; i < children.length; i++) {
                children[i] = null;
            }
            value = null;
            ids = new LinkedList<>();
        }
    }

    public Autocomplete(LinkedHashMap<Long, GraphDB.Node> nodes) {
        this.nodes = nodes;
        buildTrie();
    }

    private void buildTrie() {
        root = new TrieNode();
        Set<Long> keys = nodes.keySet();
        Iterator<Long> itr = keys.iterator();
        int cnt = 0;
        while(itr.hasNext()) {
            cnt++;
            long key = itr.next();
            if(nodes.get(key).getName() != null) {
                addTrieLocation(root, nodes.get(key).getName(), nodes.get(key).getID());
                System.out.println("the name is " + nodes.get(key).getName());
            }
        }
        System.out.println("the total number is " + cnt);
    }

    private void addTrieLocation(TrieNode root, String name, long id) {
        String cleaned = GraphDB.cleanString(name);
        TrieNode pointer = root;
        for(int i = 0; i < cleaned.length(); i++) {
            int index = cleaned.charAt(i) - 'a' >= 0 ? cleaned.charAt(i) - 'a': 26;
            //System.out.println(cleaned.charAt(i) + "and " + index);
            if(pointer.children[index] == null) {
                pointer.children[index]= new TrieNode();
            }
            pointer = pointer.children[index];
        }
        pointer.end = true;
        pointer.value = name;
        pointer.ids.add(id);
    }

    public List<Map<String, Object>> getLocationNodes(String location) {
        List<Map<String, Object>> result = new LinkedList<>();
        String cleanedLocation = GraphDB.cleanString(location);
        TrieNode pointer = root;

        for(int i = 0; i < cleanedLocation.length(); i++) {
            int index = cleanedLocation.charAt(i) - 'a' >= 0 ? cleanedLocation.charAt(i) - 'a' : 26;
            if(pointer.children[index] == null) {
                System.out.println("no matching prefix");
                return null;
            }
            pointer = pointer.children[index];
        }
        for (long curid : pointer.ids) {
            GraphDB.Node temp = nodes.get(curid);
            Map<String, Object> current = new LinkedHashMap<>();
            current.put("lat", temp.getLat());
            current.put("lon", temp.getLon());
            current.put("name", temp.getName());
            current.put("id", temp.getID());
            result.add(current);
            System.out.println("get the location: " + temp.getName());
        }
        return result;
    }

    public TrieNode getRoot() {
        return root;
    }

}
