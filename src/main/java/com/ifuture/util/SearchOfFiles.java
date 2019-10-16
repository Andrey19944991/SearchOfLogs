package com.ifuture.util;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

public class SearchOfFiles extends SimpleFileVisitor<Path> {

    private String extension; //расширение файлов

    private String text; // текст внутри файла

    private final Stack<List> levels = new Stack();

    private List<MutableTreeNode> lastLevel = new ArrayList<>();

    public SearchOfFiles(String extension, String text) {
        this.extension = extension;
        this.text = text;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

        levels.push(lastLevel);
        lastLevel = new ArrayList<>();
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        if (Files.isReadable(file) && file.toString().endsWith(extension)) {

            try (Stream<String> fileStream = Files.lines(file)) {

                if (fileStream.map(String::toLowerCase)
                        .anyMatch(s -> s.contains(text))) {

                    String fileName = file.getFileName().toString();
                    MutableTreeNode newNode
                            = new DefaultMutableTreeNode(fileName);
                    lastLevel.add(newNode);


                }
            } catch (IOException ex) {

            } finally {
                return FileVisitResult.CONTINUE;
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {

        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

        if (!lastLevel.isEmpty()) {
            String dirName = dir.getFileName().toString();
            DefaultMutableTreeNode newNode
                    = new DefaultMutableTreeNode(dirName);
            lastLevel.forEach(node -> newNode.add(node));
            newNode.setUserObject(dirName);
            lastLevel = levels.pop();
            lastLevel.add(newNode);
        } else {
            lastLevel = levels.pop();
        }

        return FileVisitResult.CONTINUE;
    }

    public TreeNode getRoot() {
        return lastLevel.isEmpty() ? null : lastLevel.get(0);
    }
}
