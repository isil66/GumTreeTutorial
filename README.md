
# GumTree Demo

Short, hands-on demo of the [GumTree](https://github.com/GumTreeDiff/gumtree) CLI & Java API.

**Docs:**

* Getting Started — [https://github.com/GumTreeDiff/gumtree/wiki/Getting-Started](https://github.com/GumTreeDiff/gumtree/wiki/Getting-Started)
* Commands — [https://github.com/GumTreeDiff/gumtree/wiki/Commands](https://github.com/GumTreeDiff/gumtree/wiki/Commands)
* GumTree API — [https://github.com/GumTreeDiff/gumtree/wiki/GumTree-API](https://github.com/GumTreeDiff/gumtree/wiki/GumTree-API)

---
## Getting started
Clone this repo and navigate to the project root to follow the tutorial.

```
git clone https://github.com/isil66/GumTreeTutorial
cd GumTreeTutorial
```
## Repo layout

```
.
├─ before/                 # demo source A
│  └─ Main.java
├─ after/                  # demo source B
│  └─ Main.java
├─ lib/                    # GumTree jars (core, client, gen.jdt, etc.)
├─ src/                    # GumTreeApiCheatSheet.java (Java API demo)
├─ apiRunner               # compiles & runs the Java demo using lib/*
└─ gumtree                 # minimal CLI wrapper: `java -cp lib/* com.github...Run "$@"`
```

---

## Demo inputs

**`before/Main.java`**

```java
public class Main {
   
    static void renameMe() { 
        System.out.println("renameMe"); 
    }

    static void deleteMe() { 
        System.out.println("deleteMe"); 
    }

    static void moveMe()   { 
        System.out.println("moveMe_top"); 
    }

    public static void main(String[] args) {
        renameMe();
        deleteMe();
        moveMe();
    }
}
```

**`after/Main.java`**

```java
public class Main {

    static void moveMe()   { 
        System.out.println("moveMe_top");
    }

    static void georgeWashington() { 
        System.out.println("renameMe"); 
    }

    public static int mathAdd(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        georgeWashington();
        mathAdd();
        moveMe();
    }
}
```

---

## Run the CLI (examples)

```bash
# Launch a browser-based diff viewer (http://localhost:4567 by default)
./gumtree webdiff before/Main.java after/Main.java
```

```bash
# Open a Swing GUI window showing the diff
./gumtree swingdiff before/Main.java after/Main.java
```

```bash
# JSON output
./gumtree textdiff -f JSON before/Main.java after/Main.java
```
```bash
# Text diff (human-readable)
./gumtree textdiff before/Main.java after/Main.java
```

```bash
# GraphViz dot (structural visualization)
./gumtree dotdiff before/Main.java after/Main.java > graph.dot
dot -Tpng graph.dot -o graph.png
```

```bash
# Parse a file into an AST
./gumtree parse before/Main.java
```

---

## Commands

* **`gumtree webdiff PATH1 PATH2`** — Launch a local web UI to visualize a diff between two files or folders.
* **`gumtree swingdiff PATH1 PATH2`** — Show the diff in a Swing desktop window.
* **`gumtree htmldiff FILE1 FILE2`** — Output an HTML diff for two files.
* **`gumtree textdiff FILE1 FILE2`** — Print a text diff (TEXT / XML / JSON formats).
* **`gumtree dotdiff FILE1 FILE2`** — Emit GraphViz DOT describing source/target ASTs and mappings.
* **`gumtree list MATCHERS|GENERATORS|PROPERTIES|CLIENTS`** — List available matchers, generators, etc.
* **`gumtree parse FILE`** — Print the AST of a file (great for understanding node kinds/offsets).

### Common options

* **`-g TREE_GENERATOR_ID`** — Force a specific parser (e.g., `java-jdt`, `java-javaparser`, `python-treesitter-ng`).
* **`-m MATCHER_ID`** — Force a specific matching algorithm (e.g., `gumtree-classic`, `gumtree-hybrid`).
* **`-M PROP VALUE`** — Tune matcher properties (e.g., `xy_minsim`, `cd_maxleaves`) for quality/speed trade-offs.
* **`-x COMMAND`** — Use an external command to produce a GumTree-compatible XML AST for each file.
* **`-f FORMAT`** *(textdiff only)* — Output format (`TEXT` default, or `XML`, `JSON`).
* **`-o FILE`** *(textdiff only)* — Write output to a file instead of stdout.
* **`--port PORT`** *(webdiff only)* — Change the web server port (default `4567`).

> Tip: Use `gumtree list GENERATORS` and `gumtree list MATCHERS` to discover IDs valid for `-g` and `-m`.

---

## Sample `textdiff` output (abridged)

```
===
update-node
---
SimpleName: renameMe [40,48]
replace renameMe by georgeWashington
===
delete-tree
---
ExpressionStatement [138,169]
    MethodInvocation [138,168]
        ...
===
insert-tree
---
ReturnStatement [252,265]
    InfixExpression [259,264]
        SimpleName: a [259,260]
        +
        SimpleName: b [263,264]
```

Offsets like `[40,48]` are **character spans** in the file. `update-node` shows renames and literal changes; `insert-tree`/`delete-tree` capture structural edits.

---
## Run the Java API demo

```bash
# Compiles to out/ and runs with jars under lib/
./apiRunner before/Main.java after/Main.java
```
---

## GumTree Java API (what this repo uses)

This repo includes a single-file “teach-by-example” demo (**`GumTreeApiCheatSheet.java`**) showing the core API steps:

1. **Initialize parsers/generators**

   ```java
   Run.initGenerators(); // registers generators found on the classpath
   ```

2. **Parse files to ASTs**

   ```java
   TreeContext beforeTc = TreeGenerators.getInstance().getTree(beforePath);
   TreeContext afterTc  = TreeGenerators.getInstance().getTree(afterPath);
   Tree beforeRoot = beforeTc.getRoot();
   Tree afterRoot  = afterTc.getRoot();
   ```

    * For Java specifically, you can force JDT:

      ```java
      Tree beforeJdt = new JdtTreeGenerator().generateFrom().file(beforePath).getRoot();
      ```

3. **Inspect / print ASTs**

   ```java
   System.out.println(beforeRoot.toTreeString());        // ad-hoc format
   System.out.println(TreeIoUtils.toLisp(beforeTc));     // LISP format
   ```

4. **Compute mappings (tree alignment)**

   ```java
   Matcher matcher = Matchers.getInstance().getMatcher();
   MappingStore mappings = matcher.match(beforeRoot, afterRoot);
   ```

5. **Compute edit script (actions)**

   ```java
   EditScriptGenerator gen = new SimplifiedChawatheScriptGenerator();
   EditScript actions = gen.computeActions(mappings);
   for (Action a : actions) System.out.println(a);
   ```

   You’ll see operations like `insert-node`, `delete-tree`, `move-tree at <index>`, `update-node`, etc.

    * `move-tree ... at N`: **N is the zero-based child index** under the target parent where the node is inserted.

---

## Troubleshooting

* **`ClassNotFoundException: com.github.gumtreediff.client.Run`** — Ensure `gumtree-client-*.jar` is present in `lib/` and your wrapper uses `-cp "lib/*"`.
* **Java version**: GumTree requires **Java 17+**.
* **Generators**: If parsing fails, try forcing a generator with `-g` (see `gumtree list GENERATORS`).
* **GraphViz**: Install `dot` to render `.dot` to images (`brew install graphviz` / `apt install graphviz`).

---
# Now Test Your Understanding with a Quick Exercise!


**Answer the questions** in this Google Form:
   [GumTree Exercises](https://forms.gle/ZsXCyk4rz188sE5N7)
   Use what you saw in your diff output (the AST nodes, the commands) to help with your answers. Also for question 2, you may wanna use the files in the exercise folder.


