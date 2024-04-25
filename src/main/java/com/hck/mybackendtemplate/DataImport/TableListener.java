package com.hck.mybackendtemplate.DataImport;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;

// TableListener 不能被 spring 管理
@Slf4j
public class TableListener implements ReadListener<ImportExcelTable> {

    /**
     * 每读一条数据就会触发
     * @param data
     * @param context
     */
    @Override
    public void invoke(ImportExcelTable data, AnalysisContext context) {
        System.out.println(data);
    }

    /**
     * 所有数据读完之后触发
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.println("数据已解析完成");
    }
}
