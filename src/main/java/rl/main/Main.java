package rl.main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    private static List<TableConfig> config;
    private static final ObjectMapper mapper = new ObjectMapper();

    static class Header {
        String name;
        int index;

        public Header(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }

    public static void main(String[] args) throws Exception {
        String fileLocation = args[0];
        if (args.length > 1) {
            initConfig(args[1]);
        }
        FileInputStream file = new FileInputStream(new File(fileLocation));
        Workbook workbook = new XSSFWorkbook(file);
        List<String> sqls = new ArrayList<>();

        workbook.sheetIterator().forEachRemaining((sheet -> {
            String table = sheet.getSheetName();
            Optional<TableConfig> tableConfig = config.stream().filter(t -> t.getTable().equals(table)).findFirst();
            System.out.println("generating " + table);
            short topRow = sheet.getTopRow();
            Row header = sheet.getRow(topRow);
            List<Header> headers = new ArrayList<>();
            for (Cell cell : header) {
                headers.add(new Header(cell.getStringCellValue(), cell.getColumnIndex()));
            }
            boolean isError = false;
            for (Row row : sheet) {
                if (row.getRowNum() == topRow) {
                    continue;
                }
                StringBuilder sql = new StringBuilder("INSERT INTO ").
                        append(table).append("(");
                sql.append(
                        headers.stream().map(h -> h.name).collect(Collectors.joining(",")));

                sql.append(") VALUES(");
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    CellType cellType = cell == null ? CellType.STRING : cell.getCellType();
                    try {
                        switch (cellType) {
                            case STRING:
                                ColumnConfig cc = getColumnConfig(tableConfig, headers, i);
                                String s = validate(cc, cell == null ? "" : cell.getStringCellValue());
                                if (cc.isGetDate()) {
                                    sql.append(" " + s + ", ");
                                } else {
                                    sql.append("'" + s + "', ");
                                }
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    Date date = cell.getDateCellValue();
                                    Instant instant = date.toInstant();
                                    String mssqlTime = instant.atOffset(ZoneOffset.UTC).format(dtf);
                                    sql.append("'" + mssqlTime + "',");
                                } else {
                                    double d = cell.getNumericCellValue();
                                    if (d % 1 == 0) {
                                        sql.append(Double.valueOf(cell.getNumericCellValue()).intValue() + ", ");
                                    } else {
                                        sql.append(cell.getNumericCellValue() + ", ");
                                    }
                                }
                                break;
                            case BOOLEAN:
                                System.out.println("todo boolean");
                                break;
                            default:
                                System.out.println("ERROR - unknown type " + cell);
                                break;
                        }
                    } catch (ValidationEception e) {
                        sqls.add("--" + e.getError());
                        isError = true;
                        break;
                    }
                }
                if (!isError) {
                    int i = sql.lastIndexOf(",");
                    sqls.add(sql.substring(0, i) + ");");
                }
            }
        }));

        for (String sql : sqls) {
            System.out.println(sql);
        }

    }

    private static ColumnConfig getColumnConfig(Optional<TableConfig> tableConfig, List<Header> headers, int i) {
        return tableConfig.isPresent() ?
                tableConfig.get().columns.stream().filter(c -> c.getName().equals(headers.get(i).name)).findFirst().orElse(null) : new ColumnConfig();
    }

    private static void initConfig(String pathToConfig) {
        try {
            config = mapper.readValue(Paths.get(pathToConfig).toFile(), new TypeReference<List<TableConfig>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(config);
    }

    private static String validate(ColumnConfig cfg, String value) throws ValidationEception {
        if (cfg == null) {
            return value;
        }
        if (cfg.isUuid()) {
            return UUID.randomUUID().toString();
        }
        if (cfg.getLength() != null) {
            if (value.length() < cfg.getLength().min) {
                throw new ValidationEception(cfg.getName(), value, "Must be at least " + cfg.getLength().min + " chars long");
            }
            if (value.length() > cfg.getLength().max) {
                throw new ValidationEception(cfg.getName(), value, "Must not be longer than " + cfg.getLength().max + " chars");
            }
        }
        if (cfg.isGetDate()) {
            return "GETDATE()";
        }
        return value;
    }
}
