package com.example.fluenti18n;

import io.github.unattendedflight.fluent.i18n.I18n;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Application {
  public static void main(String[] args) {
    handleArgs(args);
    initialize();
    System.out.println(I18n.translate("Hello and welcome!"));
    System.out.println(I18n.context("action-duck").description("Seeing a person duck")
        .translate("I saw her duck"));
    System.out.println(I18n
        .context("pet-duck")
        .description(
            "Seeing someone's pet duck"
        )
        .translate("I saw her " +
            "duck")
    );

    I18n.setCurrentLocale(Locale.forLanguageTag("fr"));
    System.out.println(I18n.translate("Hello and welcome!"));


    for (int i = 1; i <= 5; i++) {
      //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
      // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
      System.out.println("i = " + i);
    }
  }

  private static void initialize() {
    I18n.initialize();
  }

  private static void handleArgs(String[] args) {
    Iterator<String> argList = List.of(args).iterator();
    while (argList.hasNext()) {
      String arg = argList.next();
      if (arg.equals("-h") || arg.equals("--help")) {
        System.out.println("-h, --help            : Show this help message");
        System.out.println("-l, --locale <locale> : Set the locale");
      } else if (arg.equals("-l") || arg.equals("--locale")) {
        if (argList.hasNext()) {
          String locale = argList.next();
          I18n.setCurrentLocale(Locale.forLanguageTag(locale));
        } else {
          System.out.println("Missing locale argument");
          System.exit(1);
        }
      } else {
        System.out.println("Unknown argument: " + arg);
      }
    }
  }
}