package com.hck.mybackendtemplate.DataImport;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;

public class ImportExcel {
    public static void main(String[] args) {
        // 写法1：JDK8+ ,不用额外写一个 DemoDataListener
        // since: 3.0.0-beta1
        String fileName = "/Users/huchenkun/Desktop/userInfo.xlsx";
        // 监听器读取
        EasyExcel.read(fileName, ImportExcelTable.class, new PageReadListener<ImportExcelTable>(dataList -> {
            for (ImportExcelTable data : dataList) {
                System.out.println(data);
            }
        })).sheet().doReadSync();

    }
}
