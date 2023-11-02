module com.gtohelper {
    requires java.base;
    requires java.sql;
    requires java.desktop;
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.controls;

    exports com.gtohelper.datafetcher.models to javafx.graphics;

    exports com.gtohelper.datafetcher.controllers to javafx.fxml;
    opens com.gtohelper.datafetcher.controllers to javafx.fxml;

    exports com.gtohelper.datafetcher.controllers.solversettings to javafx.fxml;
    opens com.gtohelper.datafetcher.controllers.solversettings to javafx.fxml;

    opens com.gtohelper.fxml to javafx.fxml;
}
