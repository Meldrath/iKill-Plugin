package com.lsd.umc.nodeka.plugin;

import com.lsd.umc.script.ScriptInterface;
import com.lsd.umc.util.AnsiTable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class iKill {

    private ScriptInterface script;
    //( impartial, invisible ) ( AFK ) Inoedor is standing here.
    private static final Pattern target = Pattern.compile("\033\\[1;37m\\(\033\\[0m\033\\[0m\033\\[37m (immoral|moral|true impartial|impartial)(?:, (?:invisible|cloaked)){0,2} (?:.+(Comatose|AFK))?.+(?:\033\\[1;37m(.+))\033\\[37m.*(is standing here|is here, passed out|is resting here|is here, fighting)(?:(.+))*\\.\033\\[0m");
    private List<kosTarget> targets = new ArrayList<>();

    public void init(ScriptInterface script) {
        this.script = script;

        script.print(AnsiTable.getCode("yellow") + "iKill Plugin loaded.\001");
        script.registerCommand("ikill", "com.lsd.umc.nodeka.plugin.iKill", "menu");
    }

    public String menu(String args) {
        List<String> argArray = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(args);
        while (m.find()) {
            argArray.add(m.group(1).replace("\"", ""));
        }

        if (argArray.isEmpty() || argArray.size() > 4 || "".equals(argArray.get(0))) {
            this.script.capture(AnsiTable.getCode("light red") + "--------------------------------------------------------------------------\001");
            this.script.capture(AnsiTable.getCode("yellow") + "Syntax:\001");
            this.script.capture(AnsiTable.getCode("white") + " > #ikill" + AnsiTable.getCode("yellow") + " add" + AnsiTable.getCode("white") + " <target> <attack> (optional 1-10)<priority>\001");
            this.script.capture(AnsiTable.getCode("white") + " > #ikill" + AnsiTable.getCode("yellow") + " remove" + AnsiTable.getCode("white") + " <target>\001");
            this.script.capture(AnsiTable.getCode("white") + " > #ikill" + AnsiTable.getCode("yellow") + " update" + AnsiTable.getCode("white") + " <target> <attack>\001");
            this.script.capture(AnsiTable.getCode("white") + " > #ikill" + AnsiTable.getCode("yellow") + " list" + AnsiTable.getCode("white") + "\001");
            this.script.capture(AnsiTable.getCode("light red") + "--------------------------------------------------------------------------\001");
        }

        if ("add".equals(argArray.get(0))) {
            if (argArray.get(1) != null && argArray.get(2) != null && argArray.get(3) != null) {
                targets.add(new kosTarget(argArray.get(1), argArray.get(2), Integer.getInteger(argArray.get(3))));
            } else if (argArray.get(1) != null && argArray.get(2) != null && argArray.get(3) == null) {
                targets.add(new kosTarget(argArray.get(1), argArray.get(2)));
            } else {
                this.script.captureMatch(AnsiTable.getCode("light red") + "iKill: Please enter a valid target and attack.");
            }
        }

        if ("remove".equals(argArray.get(0))) {
            if (argArray.get(1) != null) {
                for (kosTarget o : targets) {
                    if (o.getTarget().equals(argArray.get(1))) {
                        targets.remove(o);
                    }
                }
            } else {
                this.script.captureMatch(AnsiTable.getCode("light red") + "iKill: Please enter a valid target for removal.");
            }
        }

        if ("update".equals(argArray.get(0))) {
            if (argArray.get(1) != null && argArray.get(2) != null) {
                for (kosTarget o : targets) {
                    if (o.getTarget().equals(argArray.get(1))) {
                        o.setAttack(argArray.get(2));
                    }
                }
            } else {
                this.script.captureMatch(AnsiTable.getCode("light red") + "iKill: Please enter a valid target for update.");
            }
        }

        if ("list".equals(argArray.get(0))) {
            for (kosTarget o : targets) {
                this.script.capture(AnsiTable.getCode("yellow") + "NAME: " + o.getTarget() + AnsiTable.getCode("white") + "ATTACK: " + o.getAttack());
            }
        }

        return "";
    }

    public void IncomingEvent(ScriptInterface event) {
        Matcher t = target.matcher(event.getEvent());
        if (t.find()) {
            for (kosTarget o : targets) {
                if (o.getTarget().equals(t.group(3))) {
                    this.script.captureMatch(AnsiTable.getCode("light red") + "iKill: Target Aquired. Preparing the laser cannons...");
                    this.script.captureMatch(AnsiTable.getCode("light red") + "iKill: ...FIRE!");
                    this.script.parse(o.getAttack() + " " + o.getTarget());
                }

            }
        }
    }
}
