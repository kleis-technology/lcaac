package com.github.albanseurat.lcaplugin.vfs

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.NewVirtualFile
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SimaproCsvVirtualFile(private val name: String, private val simaproCsvFileSystem: SimaproCsvFileSystem) :
    NewVirtualFile() {
    override fun getName(): String = name

    override fun getNameSequence(): CharSequence = name

    override fun getFileSystem(): SimaproCsvFileSystem = simaproCsvFileSystem

    override fun getPath(): String = name

    override fun isWritable(): Boolean {
        return false
    }

    override fun setWritable(writable: Boolean) {
        throw IOException("Not supported")
    }

    override fun isDirectory(): Boolean {
        return true
    }

    override fun getCanonicalFile(): NewVirtualFile? {
        return this
    }

    override fun getParent(): NewVirtualFile? {
        return null
    }

    override fun getChildren(): Array<VirtualFile> {
        return emptyArray()
    }

    override fun findChild(name: String): NewVirtualFile? {
        return null
    }

    override fun getOutputStream(requestor: Any?, newModificationStamp: Long, newTimeStamp: Long): OutputStream {
        throw IOException("Not supported")
    }

    override fun getTimeStamp(): Long {
        return 0;
    }

    override fun getLength(): Long {
        return 0;
    }

    override fun getInputStream(): InputStream {
        throw IOException("Not supported")
    }

    override fun getId(): Int {
        return 1;
    }

    override fun refreshAndFindChild(name: String): NewVirtualFile? {
        return null;
    }

    override fun findChildIfCached(name: String): NewVirtualFile? {
        return null;
    }

    override fun setTimeStamp(time: Long) {
        TODO("Not yet implemented")
    }

    override fun markDirty() {
        TODO("Not yet implemented")
    }

    override fun markDirtyRecursively() {
        TODO("Not yet implemented")
    }

    override fun isDirty(): Boolean {
        return false;
    }

    override fun markClean() {
        TODO("Not yet implemented")
    }

    override fun getCachedChildren(): Collection<VirtualFile> {
        return emptyList()
    }

    override fun iterInDbChildren(): Iterable<VirtualFile> {
        return emptyList()
    }

    override fun isValid(): Boolean = true

    override fun exists(): Boolean = true
}