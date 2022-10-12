package com.github.albanseurat.lcaplugin.vfs

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.impl.ArchiveHandler
import com.intellij.openapi.vfs.newvfs.ArchiveFileSystem


/**
 * Allow to have one file (simapro csv to represent a whole library)
 */
class SimaproCsvFileSystem : ArchiveFileSystem() {

    override fun getProtocol(): String = "scsv"

    override fun findFileByPath(path: String): VirtualFile? {
        TODO("Not yet implemented")
    }

    override fun refresh(asynchronous: Boolean) {
        TODO("Not yet implemented")
    }

    override fun refreshAndFindFileByPath(path: String): VirtualFile? {
        TODO("Not yet implemented")
    }

    override fun extractRootPath(normalizedPath: String): String {
        TODO("Not yet implemented")
    }

    override fun findFileByPathIfCached(path: String): VirtualFile? {
        TODO("Not yet implemented")
    }

    override fun extractLocalPath(rootPath: String): String {
        TODO("Not yet implemented")
    }

    override fun composeRootPath(localPath: String): String {
        TODO("Not yet implemented")
    }

    override fun getHandler(entryFile: VirtualFile): ArchiveHandler {
        TODO("Not yet implemented")
    }

}