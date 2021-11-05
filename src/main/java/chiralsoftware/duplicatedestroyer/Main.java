package chiralsoftware.duplicatedestroyer;

import com.google.common.hash.HashFunction;
import static com.google.common.hash.Hashing.sha256;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import static java.lang.System.out;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Really simple method to remove duplicates found in the second directory */
public final class Main {
    
    private static final Map<String,File> fileHashes = new HashMap<>();
    
    private static final HashFunction hf = sha256();
    
    private final static List<File> deletedFiles = new ArrayList<>();
    
    private static long count = 0;
    
    private static final FileVisitor<Path> deleteVisitor = new FileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final File f = file.toFile();
            if(! f.canRead()) return CONTINUE; 
            if(! f.isFile()) return CONTINUE; 
            final String hashString = Files.asByteSource(f).hash(hf).toString();
            if(fileHashes.containsKey(hashString)) {
                deletedFiles.add(f);
                f.delete();
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return CONTINUE;
        }
    
};
    
    private static final List<File> preserveDuplicatesList = new ArrayList<>();
    
    private static final FileVisitor<Path> preserveVisitor = new FileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final File f = file.toFile();
            if(! f.canRead()) return CONTINUE; 
            if(! f.isFile()) return CONTINUE; 
            count++;
            if(count % 1000 == 0) {
                out.println(count + ": " + f);
            }
            final String hashString = Files.asByteSource(f).hash(hf).toString();
            // this is a lower case hex string representing the hash
            if(Main.fileHashes.containsKey(hashString)) {
//                out.println("File " + f + " is a duplicate of: " + Main.fileHashes.get(hashString));
                preserveDuplicatesList.add(f);
                return CONTINUE;
            }
            Main.fileHashes.put(hashString, f);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return CONTINUE;
        }
        
    };
    
    public static void main(String[] args) throws Exception {
        final File preserve = new File(args[0]);
        final File check = new File(args[1]);
        out.println("Preserve directory: " + preserve);
        out.println("Check directory: " + check);
        out.println();
        out.println("Scanning the preserve directory");
        java.nio.file.Files.walkFileTree(preserve.toPath(), preserveVisitor);
        out.println(fileHashes.size() + " files found in preserve directory, excluding " + preserveDuplicatesList.size() + " duplicates");
        
        out.println("checking duplicates");
        java.nio.file.Files.walkFileTree(check.toPath(), deleteVisitor);
        out.println("deleted these files: "  + (deletedFiles.size() < 200 ? deletedFiles : deletedFiles.size()));
        out.println("Done.");
    }
    
}
