package com.torquescript.symbols;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.torquescript.TSFile;
import com.torquescript.TSFileType;
import com.torquescript.TSGuiFileType;
import com.torquescript.psi.TSFnDeclStmt;
import com.torquescript.psi.TSPackageDecl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TSFunctionCachedListGenerator extends TSCachedListGenerator<TSFnDeclStmt> {
    @Override
    public Set<TSFnDeclStmt> generate(Project project) {
        Set<TSFnDeclStmt> items = new HashSet<>();
        //Search every file in the project
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, TSFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        virtualFiles.addAll(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, TSGuiFileType.INSTANCE, GlobalSearchScope.projectScope(project)));
        for (VirtualFile virtualFile : virtualFiles) {
            TSFile tsFile = (TSFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (tsFile != null) {
                try {
                    TSFnDeclStmt[] functions = PsiTreeUtil.getChildrenOfType(tsFile, TSFnDeclStmt.class);
                    if (functions != null) {
                        Collections.addAll(items, functions);
                    }

                    TSPackageDecl[] packages = PsiTreeUtil.getChildrenOfType(tsFile, TSPackageDecl.class);
                    if (packages != null) {
                        for (TSPackageDecl pack : packages) {
                            functions = PsiTreeUtil.getChildrenOfType(pack, TSFnDeclStmt.class);
                            if (functions != null) {
                                Collections.addAll(items, functions);
                            }
                        }
                    }
                } catch (Exception ignored) {

                }
            }
        }
        return items;
    }
}
