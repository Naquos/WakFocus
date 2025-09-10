package com.wakfocus.utils;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class RuleSets {
    public static final List<ColorRule> TIMELINE = Arrays.asList(
        new ColorRule(new Color(219, 177, 115), 30, 50),
        new ColorRule(new Color(59, 60, 48), 7, 50)
    );

    public static final List<ColorRule> TIMELINE_OPPONENT = Arrays.asList(
        new ColorRule(new Color(229,117,68), 30, 50)
    );

    public static final List<ColorRule> TIMELINE_ALLIES = Arrays.asList(
        new ColorRule(new Color(68,155,219), 30, 50)
    );

    public static final List<ColorRule> PA_PM = Arrays.asList(
        new ColorRule(new Color(11, 145, 227), 30, 20),
        new ColorRule(new Color(32, 126, 5), 30, 20),
        new ColorRule(new Color(255, 255, 255), 30, 20)
    );

    public static final List<ColorRule> PA_PM_VELOCITE = Arrays.asList(
        new ColorRule(new Color(6, 47, 75), 7, 20),
        new ColorRule(new Color(31, 59, 32), 7, 20),
        new ColorRule(new Color(63, 74, 78), 7, 20)
    );

    public static final List<ColorRule> BOUTIQUE = Arrays.asList(
        new ColorRule(new Color(103, 93, 73), 10, 20)
    );
}
