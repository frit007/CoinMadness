module org.openjfx {
    requires javafx.controls;
    requires common;
    requires java.sql;
    exports org.coin_madness;
    exports org.coin_madness.screens;
    exports org.coin_madness.helpers;
    exports org.coin_madness.model;
    exports org.coin_madness.components;
    exports org.coin_madness.controller;
}