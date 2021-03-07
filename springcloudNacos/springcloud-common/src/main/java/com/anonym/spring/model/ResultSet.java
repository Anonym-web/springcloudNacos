package com.anonym.spring.model;

import java.io.Serializable;

/**
 * @author 王培忠
 * @date 2020/4/17 15:15
 * @email 805705089@qq.com
 * @Description
 * @Reason ADDREASON
 * @since JDK 1.8
 */
public class ResultSet<T> implements Serializable {

    private String RetCode;

    private String RetVal;

    private T DataRows;

    public T getDataRows() {
        return DataRows;
    }

    public void setDataRows(T dataRows) {
        DataRows = dataRows;
    }

    public String getRetCode() {
        return RetCode;
    }

    public void setRetCode(String retCode) {
        RetCode = retCode;
    }

    public String getRetVal() {
        return RetVal;
    }

    public void setRetVal(String retVal) {
        RetVal = retVal;
    }


}
