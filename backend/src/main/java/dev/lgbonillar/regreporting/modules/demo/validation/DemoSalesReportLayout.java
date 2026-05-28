package dev.lgbonillar.regreporting.modules.demo.validation;

import java.util.List;

public final class DemoSalesReportLayout {

    public static final String EXPECTED_SHEET_NAME = "Hoja1";
    public static final int HEADER_ROW_INDEX = 0;

    public static final List<String> EXPECTED_HEADERS = List.of(
            "ID Cliente",
            "Zona",
            "País",
            "Tipo de producto",
            "Canal de venta",
            "Prioridad",
            "Fecha pedido",
            "ID Pedido",
            "Fecha envío",
            "Unidades",
            "Precio Unitario",
            "Coste unitario",
            "Importe venta total",
            "Importe Coste total"
    );

    private DemoSalesReportLayout() {
    }

    public static int columnIndex(String header) {
        return EXPECTED_HEADERS.indexOf(header);
    }
}
