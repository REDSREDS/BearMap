import java.util.*;

public class Autocomplete {

//    declare a root TrieNode for autocomplete
    private TrieNode root = new TrieNode();

    /**
     * create TrieNode class
     */
    class TrieNode {
        TrieNode[] links;
        boolean exist;

        TrieNode() {
            links = new TrieNode[128];
            exist = false;
        }
    }

    public Autocomplete(LinkedHashMap<Long, GraphDB.Node> nodes) {
        init(nodes);
    }

//    put all names in nodes into the trie
    public void init(LinkedHashMap<Long, GraphDB.Node> nodes) {
        for(GraphDB.Node toPut : nodes.values()) {
            if(toPut.getName() != null) {
                put(GraphDB.cleanString(toPut.getName()));
            }
        }
    }

//    for test: put a list of names into the trie
    public void init(String[] names) {
        for(String name : names) {
            put(name);
//            System.out.println("name "+ name + " has been put");
        }
    }


//    put a name into a trie
    private void put(String name) {
        put(root, name, 0);
    }

//    helper class: put one character of the name into a trie recursively
//    until the whole name is put.
    private TrieNode put(TrieNode current, String name, int p) {
        if(current == null) {
            current = new TrieNode();
        }

        if(p == name.length()) {
            current.exist = true;
            return current;
        }

        char character = name.charAt(p);
        current.links[character] = put(current.links[character], name, p + 1);
        return current;
    }

    public List<String> getNameByPrefix(String prefix) {
        ArrayList<String> result = new ArrayList<>();
        TrieNode search = root;
        char[] pre = prefix.toCharArray();
        for (char i : pre) {
            if (search.links[i] != null) {
                search = search.links[i];
            } else {
                return result;
            }
        }
        addNameByPrefix(search,prefix,result);
        return result;
    }

    private void addNameByPrefix(TrieNode search,String prefix, List<String> result) {

        if(search.exist == true) {
            result.add(prefix);
        }
        for(int i = 0; i < 128; i++) {
            if(search.links[i] != null) {
                addNameByPrefix(search.links[i], prefix + (char)i, result);
            }
        }
    }
}
