package Connector;

import Model.MemberPallete;
import Util.ListTable;
import Util.Table;

import java.util.List;

public class Test {

    public static void main(String[] args) {
        // Definition
        ListTable<MemberPallete> userListTable = new ListTable<>(List.of("Name", "Hex Color", "BBGGRR Color"),
                object -> List.of(object.getMemberName(), object.getHexColor(), object.getBgrColor()));

        userListTable.add(new MemberPallete("Aki", "#4982FE"));
        userListTable.add(new MemberPallete("Ayame", "#9C3741"));
        userListTable.add(new MemberPallete("Hajime", "#B6B9FF"));
//        userListTable.add(new MemberPallete("Aki", "#4982FE"));

// Print the Table
        Table table = userListTable.getTable();
        System.out.println(table);

        table.setAlign(Table.ALIGN_LEFT);
        System.out.println(table);
    }

}
