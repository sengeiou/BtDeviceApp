package com.cmtech.android.bledevice.ecgmonitor.ecgfile;

import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.DataIOUtil;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.cmtech.bmefile.exception.FileException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EcgFileComment {
    public static final int COMMENTATOR_LEN = 10;
    public static final int COMMENT_LEN = 100;

    private String commentator = "匿名";
    private long commentTime;
    private String comment = "无内容";

    public String getCommentator() {
        return commentator;
    }

    public long getCommentTime() {
        return commentTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public EcgFileComment() {

    }

    public EcgFileComment(String commentator, long commentTime, String comment) {
        this.commentator = commentator;
        this.commentTime = commentTime;
        this.comment = comment;
    }

    public void readFromStream(DataInput in) throws FileException {
        try {
            commentator = DataIOUtil.readFixedString(COMMENTATOR_LEN, in);
            commentTime = ByteUtil.reverseLong(in.readLong());
            comment = DataIOUtil.readFixedString(COMMENT_LEN, in);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "读心电文件头错误");
        }
    }

    public void writeToStream(DataOutput out) throws FileException {
        try {
            DataIOUtil.writeFixedString(commentator, COMMENTATOR_LEN, out);
            out.writeLong(ByteUtil.reverseLong(commentTime));
            DataIOUtil.writeFixedString(comment, COMMENT_LEN, out);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileException("", "写心电文件头错误");
        }
    }

    public static int getLength() {
        return  8 + 2*(COMMENTATOR_LEN +COMMENT_LEN);
    }

    @Override
    public String toString() {
        return commentator +
                "在" + DateTimeUtil.timeToShortStringWithTodayYesterdayFormat(commentTime) +
                "说：" + comment + '\n';
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;

        EcgFileComment otherComment = (EcgFileComment)otherObject;

        return  (commentator.equals(otherComment.commentator) && (commentTime == otherComment.commentTime));
    }

    @Override
    public int hashCode() {
        return (int)(commentator.hashCode() + commentTime);
    }
}