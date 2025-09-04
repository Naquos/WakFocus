package com.wakfocus;

import java.io.IOException;

import com.wakfocus.UI.WakFocusUI;
import com.wakfocus.services.FocusService;

public class WakFocus {
    public static void main(String[] args) throws InterruptedException, IOException {
        try {
            FocusService.run();
            WakFocusUI.launchUI(args);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

}