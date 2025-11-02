package com.example.quanlytoanha;

public class Launcher {
    public static void main(String[] args) {
        // Check if we want to run TestResidentDashboard
        if (args.length > 0 && args[0].equals("com.example.quanlytoanha.TestResidentDashboard")) {
            TestResidentDashboard.main(new String[0]);
        } else {
            Main.main(args);
        }
    }
}




