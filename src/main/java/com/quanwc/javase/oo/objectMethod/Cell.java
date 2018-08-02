package com.quanwc.javase.oo.objectMethod;

import java.util.Objects;

/**
 * Object类：
 *      toString()  getClass().getName() + "@" + Integer.toHexString(hashCode())
 *      hashCode()  返回一个int整数，用来描述对象的内存地址
 *      equals()    return (this == obj)
 *
 *
 *
 * Created by quanwenchao
 * 2018/7/31 11:07:35
 */
public class Cell {
    /** 行 */
    private Integer row;
    /** 列 */
    private Integer col;

    public Cell(Integer row, Integer col) {
        this.row = row;
        this.col = col;
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * 重写Object中的equals
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == null) {
            return true;
        }

        //判断对象的所属类型, 相同的实例才有可比性
        if (obj instanceof Cell) {
            Cell cell = (Cell) obj;
            return this.row == cell.row && this.col == cell.col;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }

    public static void main(String[] args) {

        Cell cell1 = new Cell(1, 2);
        System.out.println(cell1.hashCode()); // 783286238
        System.out.println(cell1.toString()); // com.quanwc.javase.oo.objectMethod.Cell@2eafffde

        Cell cell2 = new Cell(1, 2);
        System.out.println(cell2.equals(cell1));

    }

}
