import java.nio.file.*;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.TreeGenerators;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.Action;

public class GumTreeApiCheatSheet {

    public static void main(String[] args) throws Exception {
        // You can hardcode your paths here if you don’t want to pass args:
        String beforePath = args.length > 0 ? args[0] : "before/Main.java";
        String afterPath  = args.length > 1 ? args[1] : "after/Main.java";

        requireExists(beforePath);
        requireExists(afterPath);

        // ------------------------------------------------------------
        // ----- Init generators -----
        // ------------------------------------------------------------
        System.out.println("----- Init generators -----");
        Run.initGenerators();
        System.out.println("Generators initialized.");
        System.out.println();

        // ------------------------------------------------------------
        // ----- Parse (default generator) -----
        // ------------------------------------------------------------
        System.out.println("----- Parse (default generator) -----");
        TreeContext beforeTc = TreeGenerators.getInstance().getTree(beforePath);
        TreeContext afterTc  = TreeGenerators.getInstance().getTree(afterPath);
        Tree beforeRoot = beforeTc.getRoot();
        Tree afterRoot  = afterTc.getRoot();
        System.out.println("Parsed (default):");
        System.out.println("  BEFORE root: " + labelOf(beforeRoot));
        System.out.println("  AFTER  root: " + labelOf(afterRoot));
        System.out.println();

        // Optional: show trees
        System.out.println("Tree BEFORE (ad-hoc):");
        System.out.println(beforeRoot.toTreeString());
        System.out.println("Tree AFTER  (ad-hoc):");
        System.out.println(afterRoot.toTreeString());
        System.out.println("Trees in LISP (BEFORE then AFTER):");
        System.out.println(TreeIoUtils.toLisp(beforeTc).toString());
        System.out.println(TreeIoUtils.toLisp(afterTc).toString());
        System.out.println();

        // ------------------------------------------------------------
        // ----- Parse with a specific generator (JDT) when Java -----
        // ------------------------------------------------------------
        if (isJava(beforePath) && isJava(afterPath)) {
            System.out.println("----- Specific generator (JDT) for .java files -----");
            Tree beforeJdt = new JdtTreeGenerator().generateFrom().file(beforePath).getRoot();
            Tree afterJdt  = new JdtTreeGenerator().generateFrom().file(afterPath).getRoot();
            System.out.println("JDT roots:");
            System.out.println("  BEFORE: " + labelOf(beforeJdt));
            System.out.println("  AFTER : " + labelOf(afterJdt));
            System.out.println();
        }

        // ------------------------------------------------------------
        // ----- Compute mappings (default matcher) -----
        // ------------------------------------------------------------
        System.out.println("----- Compute mappings (default matcher) -----");
        Matcher matcher = Matchers.getInstance().getMatcher();
        MappingStore mappings = matcher.match(beforeRoot, afterRoot);
        System.out.println("Total mappings: " + mappings.size());
        System.out.println("Some mappings (src -> dst):");
        printSomeMappings(mappings, 12);
        System.out.println();

        // ------------------------------------------------------------
        // ----- Compute edit script (Simplified Chawathe) -----
        // ------------------------------------------------------------
        System.out.println("----- Compute edit script (SimplifiedChawathe) -----");
        EditScriptGenerator gen = new SimplifiedChawatheScriptGenerator();
        EditScript actions = gen.computeActions(mappings);
        System.out.println("Number of actions: " + actions.size());
        System.out.println("Actions:");
        for (Action a : actions) {
            System.out.println("  " + a);
        }
        System.out.println();

        System.out.println("----- Done -----");
    }

    private static void requireExists(String p) throws Exception {
        if (!Files.exists(Path.of(p))) {
            throw new IllegalArgumentException("File not found: " + p);
        }
    }

    private static boolean isJava(String p) {
        String s = p.toLowerCase();
        return s.endsWith(".java");
    }

    private static String labelOf(Tree t) {
        return t.getType().name + "(" + t.getLabel() + ")";
    }

    private static void printSomeMappings(MappingStore mappings, int limit) {
        int i = 0;
        for (var m : mappings) {
            if (i++ >= limit) break;
            Tree s = (Tree) m.first;
            Tree d = (Tree) m.second;
            System.out.println("  " + labelOf(s) + "  ->  " + labelOf(d));
        }
    }
}
