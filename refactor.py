import os

project_root = r"c:\Users\tdmca\OneDrive\Desktop\OOP\Sims-CLI-game\src\simcli"

for root, dirs, files in os.walk(project_root):
    if "ui" in root:
        continue
    for f in files:
        if f.endswith(".java"):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as file:
                content = file.read()
            
            original_content = content
            
            # replace System.out.println
            content = content.replace("System.out.println", "simcli.ui.UIManager.printMessage")
            # replace System.out.print
            content = content.replace("System.out.print", "simcli.ui.UIManager.prompt")
            # replace System.err.println
            content = content.replace("System.err.println", "simcli.ui.UIManager.printWarning")
            
            if content != original_content:
                with open(path, 'w', encoding='utf-8') as file:
                    file.write(content)
                print(f"Updated {f}")

print("Done replacing.")
