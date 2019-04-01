package com.cmtech.bmefile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import static java.nio.ByteOrder.BIG_ENDIAN;

/**
 * BmeFile: Bme文件
 * created by chenm, 2018-02-11
 */

public abstract class BmeFile {
	//protected static Set<String> fileInOperation = new HashSet<>(); // 已经打开的文件列表
	protected static final BmeFileHead DEFAULT_BMEFILE_HEAD = BmeFileHeadFactory.createDefault(); // 缺省文件头
	
	private static final byte[] BME = {'B', 'M', 'E'}; // BmeFile标识符
	
	protected File file; // 文件
	protected DataInput in; // 输入流
	protected DataOutput out; // 输出流
	protected final BmeFileHead fileHead; // 文件头
	protected int dataNum; // 文件包含的数据个数
    public int getDataNum() {
        return dataNum;
    }

    // 为已存在文件生成BmeFile
	protected BmeFile(String fileName) throws IOException{
        File tempfile = new File(fileName);
        if(tempfile.exists() && tempfile.renameTo(tempfile)) {
            file = tempfile;
            fileHead = open(file);
            if(fileHead == null) {
                throw new IOException("打开文件错误:" + fileName);
            }
        } else {
            throw new IOException("打开文件错误:" + fileName);
        }
	}

	// 为不存在的文件创建BmeFile
	protected BmeFile(String fileName, BmeFileHead head) throws IOException{
        File tempfile = new File(fileName);
        if(!tempfile.exists()) {
            file = tempfile;
            fileHead = create(head);
            if(fileHead == null) {
                throw new IOException("创建文件错误:" + fileName);
            }
        } else {
            throw new IOException("创建文件错误:" + fileName);
        }
	}

    // 判断文件是否在操作中
    public boolean isActive() {
        if(file == null) return false;
        if(!file.exists()) return false;
        return !file.renameTo(file);
    }

	private BmeFileHead open(File file){
		BmeFileHead fileHead;
		
		if(file == null)
			throw new IllegalArgumentException();
        if(!file.exists() || in != null || out != null)
            throw new IllegalStateException();

		try	{
            if(!createInputStream())
                return null;
			byte[] bme = new byte[3];
			in.readFully(bme); // 读BmeFile标识符
			if(!Arrays.equals(bme, BME))
			    return null;
			byte[] ver = new byte[2];
			in.readFully(ver); // 读版本号
			fileHead = BmeFileHeadFactory.create(ver);
			if(!fileHead.readFromStream(in)) // 读BmeFileHead
			    return null;
		} catch (IOException e) {
			return null;
		}
		return fileHead;
	}

    private BmeFileHead create(BmeFileHead head){
        if(head == null)
            throw new IllegalArgumentException();
        if(file == null || in != null || out != null)
            throw new IllegalStateException();

        try {
            if(file.exists()) {
                if(!file.delete()) {
                    throw new IOException();
                }
            }
            if(!file.createNewFile()) {
                throw new IOException();
            }
            if(!createOutputStream())
                throw new IOException();
            out.write(BME); // 写BmeFile文件标识符
            out.write(head.getVersion()); // 写版本号
            if(!head.writeToStream(out)) { // 写BmeFileHead
                throw new IOException();
            }
        } catch(IOException ioe) {
            return null;
        }
        return head;
    }

    protected abstract boolean createInputStream();
    protected abstract boolean createOutputStream();
	protected abstract int availableData();
    protected abstract boolean isEof() throws IOException;
    public abstract void close() throws IOException;



    // 读单个byte数据
    public byte readByte() throws IOException{
        return in.readByte();
    }

    // 读单个int数据
    public int readInt() throws IOException {
        return readInt(in);
    }

    // 读单个double数据
    public double readDouble() throws IOException{
        return readDouble(in);
    }

    // 写单个byte数据
    public void writeData(byte data) throws IOException{
        out.writeByte(data);
        dataNum++;
    }

    // 写单个int数据
    public void writeData(int data) throws IOException{
        writeInt(out, data);
        dataNum++;
    }

    // 写单个double数据
    public void writeData(double data) throws IOException{
        writeDouble(out, data);
        dataNum++;
    }

    // 写byte数组
    public void writeData(byte[] data) throws IOException{
        for(byte num : data) {
            writeData(num);
        }
    }

    // 写int数组
    public void writeData(int[] data) throws IOException{
        for(int num : data) {
            writeData(num);
        }
    }

    // 写double数组
	public void writeData(double[] data) throws IOException{
        for(double num : data) {
            writeData(num);
        }
	}

    private int readInt(DataInput in) throws IOException{
        return (fileHead.getByteOrder() == BIG_ENDIAN) ? in.readInt() : ByteUtil.reverseInt(in.readInt());
    }

    private void writeInt(DataOutput out, int data) throws IOException{
        if ((fileHead.getByteOrder() == BIG_ENDIAN)) {
            out.writeInt(data);
        } else {
            out.writeInt(ByteUtil.reverseInt(data));
        }
    }

    private double readDouble(DataInput in) throws IOException{
        return (fileHead.getByteOrder() == BIG_ENDIAN) ? in.readDouble() : ByteUtil.reverseDouble(in.readDouble());
    }

    private void writeDouble(DataOutput out, double data) throws IOException{
        if ((fileHead.getByteOrder() == BIG_ENDIAN)) {
            out.writeDouble(data);
        } else {
            out.writeDouble(ByteUtil.reverseDouble(data));
        }
    }

	public File getFile() {
	    return file;
    }

	public String getFileName() {
		if(file == null) return "";
		return file.toString();
	}
	
	public String getInfo() {
		if(fileHead == null) return "";
		return fileHead.getInfo();
	}

	public BmeFileDataType getDataType() {
		if(fileHead == null) return null;
		return fileHead.getDataType();
	}

	public int getFs() {
		if(fileHead == null) return -1;
		return fileHead.getFs();
	}
	
	public byte[] getVersion() {
		if(fileHead == null) return null;
		return fileHead.getVersion();
	}
	
	public ByteOrder getByteOrder() {
		if(fileHead == null) return null;
		return fileHead.getByteOrder();
	}
	
	public BmeFileHead getBmeFileHead() {
		return fileHead;
	}

	@Override
	public String toString() {
		return "[文件名：" + getFileName() + ":"+ fileHead + "; 数据个数：" + getDataNum() + "]";
	}
}
