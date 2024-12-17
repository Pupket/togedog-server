package pupket.togedogserver.global.trie;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Trie {
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord;
    }

    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    // 단어 삽입
    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isEndOfWord = true;
    }

    // 접두사로 시작하는 단어를 모두 찾기
    public List<String> searchByPrefix(String prefix, int limit) {
        List<String> results = new ArrayList<>();
        TrieNode node = root;

        // 접두사까지 이동
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return results; // 접두사가 없는 경우 빈 결과 반환
            }
            node = node.children.get(c);
        }

        // 접두사 이후 단어 수집 (DFS)
        dfs(node, new StringBuilder(prefix), results, limit);
        return results;
    }

    private void dfs(TrieNode node, StringBuilder prefix, List<String> results, int limit) {
        if (results.size() >= limit) return;

        if (node.isEndOfWord) results.add(prefix.toString());

        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            dfs(entry.getValue(), prefix, results, limit);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    // 단어 삭제
    public void remove(String word) {
        remove(root, word, 0);
    }

    private boolean remove(TrieNode current, String word, int index) {
        if (index == word.length()) {
            // 단어 끝에 도달하면 isEndOfWord 플래그를 false로 설정
            if (!current.isEndOfWord) {
                return false; // 단어가 존재하지 않음
            }
            current.isEndOfWord = false;
            return current.children.isEmpty(); // 자식 노드가 없으면 부모 노드에서 제거할 수 있도록 true 반환
        }

        char c = word.charAt(index);
        TrieNode node = current.children.get(c);
        if (node == null) {
            return false; // 단어가 존재하지 않음
        }

        // 재귀적으로 자식 노드 확인
        boolean shouldDeleteCurrentNode = remove(node, word, index + 1);

        // 자식 노드를 삭제할 수 있다면 현재 노드에서 해당 문자 제거
        if (shouldDeleteCurrentNode) {
            current.children.remove(c);
            return current.children.isEmpty() && !current.isEndOfWord;
        }

        return false;
    }
}