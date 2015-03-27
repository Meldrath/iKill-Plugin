package com.lsd.umc.nodeka.plugin;

import com.lsd.umc.script.ScriptInterface;
import com.lsd.umc.util.AnsiTable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class iKill {

    private ScriptInterface script;
    //( impartial, invisible ) ( AFK ) Inoedor is standing here.
    private static final Pattern standardKOSRegex = Pattern.compile("(?:\033\\[1;37m\\( (immoral|moral|true impartial|impartial)(?:, (?:invisible|cloaked)){0,2} \\)\\s?(?:\\( (Comatose|AFK) \\))?)?.+(?:\033\\[1;37m(.+))\033\\[37m.*?(is standing here|is here, passed out|is resting here|is here, fighting)(?:(.+))?\\.\033\\[0m");
    //<OG> Darsinius, Still White Kimbo fluctuates and streams into this place.
    private static final Pattern enhancedKOSRegex = Pattern.compile("\033\\[1;3(?:6|7)m(.+) (?:appears with a flashing light|has arrived|fluctuates and streams into this place)\\.\033\\[0m");
    //leaves (?:north|south|east|west|up|down)
    private final List<kosTarget> targets = new ArrayList<>();
    private final LinkedList<String> nonCombatLines = new LinkedList();
    /*
     [L:0] R:Draalik X:2000000000 G:205023444 A:-372
     [Lag: 0] [Reply: guide] [Align: 797]
     L:0 R: X:442159262
     [L:0] [Ocellaris H:43028/43028 M:8884/8884 S:5001/5001 E:21049/21049] [A:-1000] []
     */
    private static final Pattern nonCombatPrompt = Pattern.compile("^(?:\\[?(?:Lag|L):\\s?\\d+\\]?)\\s\\[?(?:R|Reply|.+ H):?\\s?");
    /*
     [L:0] Ocellaris: (perfect condition) vs. roadrunner: (badly wounded)
     [Lag: 2000] [Coil: (perfect condition)] [novice healer: (covered in blood)]

     [L:0] [Darth H:61211/63111 M:16074/16074 S:15390/15888 E:52997/54001] [A:1000] []
     [Ocellaris](perfect condition) [Bayeshi guard](near death)
     */
    private static final Pattern combatPrompt = Pattern.compile("^(?:\\[?(?:Lag|L):\\s?\\\\d+\\]?)?\\s?(?:.+):\\s?\\((?:.+)\\)|\\[.+]\\(.+\\)\\s");
    private int defaultPriority = 100;
    private int currentPriority = 100;
    private boolean standardKOS = true;
    private boolean enhancedKOS = true;
    private boolean promptState = false;
    private boolean kosGrab = false;

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
            this.script.capture(AnsiTable.getCode("white") + " > #ikill" + AnsiTable.getCode("yellow") + " add" + AnsiTable.getCode("white") + " <target> <attack> (optional 1-100)<priority>\001");
            this.script.capture(AnsiTable.getCode("white") + " > #ikill" + AnsiTable.getCode("yellow") + " remove" + AnsiTable.getCode("white") + " <target>\001");
            this.script.capture(AnsiTable.getCode("white") + " > #ikill" + AnsiTable.getCode("yellow") + " update" + AnsiTable.getCode("white") + " <target> <attack>\001");
            this.script.capture(AnsiTable.getCode("white") + " > #ikill" + AnsiTable.getCode("yellow") + " list" + AnsiTable.getCode("white") + "\001");
            this.script.capture(AnsiTable.getCode("white") + " > #ikill" + AnsiTable.getCode("yellow") + " enhanced" + AnsiTable.getCode("white") + " <on|off> (visits/recalls/spanning fluctuation/teleport)\001");
            this.script.capture(AnsiTable.getCode("white") + " > #ikill" + AnsiTable.getCode("yellow") + " on|off" + AnsiTable.getCode("white") + "\001");
            this.script.capture(AnsiTable.getCode("light red") + "--------------------------------------------------------------------------\001");
            return "";
        }

        if ("add".equals(argArray.get(0))) {
            if (argArray.get(1) != null && argArray.get(2) != null && argArray.size() == 3) {
                this.targets.add(new kosTarget(argArray.get(1).toLowerCase(), argArray.get(2), defaultPriority));
                this.script.capture(AnsiTable.getCode("white") + "iKill: " + AnsiTable.getCode("yellow")
                        + argArray.get(1) + AnsiTable.getCode("white") + " has been added with attack " + AnsiTable.getCode("cyan")
                        + argArray.get(2) + AnsiTable.getCode("white") + " and the default priority of " + AnsiTable.getCode("light red")
                        + defaultPriority + AnsiTable.getCode("white") + ".");
                defaultPriority--;
            } else if (argArray.get(1) != null && argArray.get(2) != null && argArray.get(3) != null) {
                this.targets.add(new kosTarget(argArray.get(1).toLowerCase(), argArray.get(2), Integer.parseInt(argArray.get(3))));
                this.script.capture(AnsiTable.getCode("white") + "iKill: " + AnsiTable.getCode("yellow")
                        + argArray.get(1) + AnsiTable.getCode("white") + " has been added with attack " + AnsiTable.getCode("cyan")
                        + argArray.get(2) + AnsiTable.getCode("white") + " and the priority of " + AnsiTable.getCode("light red")
                        + argArray.get(3) + AnsiTable.getCode("white") + ".");
            }
        }

        if ("remove".equals(argArray.get(0))) {
            if (argArray.size() == 2) {
                for (kosTarget o : this.targets) {
                    if (o.getTarget().toLowerCase().equals(argArray.get(1).toLowerCase())) {
                        this.targets.remove(o);
                        this.script.capture(AnsiTable.getCode("white") + "iKill: " + AnsiTable.getCode("yellow")
                                + argArray.get(1) + AnsiTable.getCode("white") + " has been removed.");
                    }
                }
            }
        }

        if ("update".equals(argArray.get(0))) {
            if (argArray.get(1) != null && argArray.get(2) != null) {
                for (kosTarget o : this.targets) {
                    if (o.getTarget().equals(argArray.get(1).toLowerCase())) {
                        o.setAttack(argArray.get(2));
                        this.script.capture(AnsiTable.getCode("white") + "iKill: " + AnsiTable.getCode("yellow")
                                + argArray.get(1) + AnsiTable.getCode("white") + " has been updated to attack: " + AnsiTable.getCode("cyan")
                                + argArray.get(2) + AnsiTable.getCode("white") + ".");
                    }
                }
            }
        }

        if ("list".equals(argArray.get(0))) {
            this.script.capture(AnsiTable.getCode("white") + "iKill: Targets");
            this.script.capture(AnsiTable.getCode("light red") + "--------------------------------------------------------------------------\001");
            for (kosTarget o : this.targets) {
                this.script.capture(AnsiTable.getCode("yellow") + "NAME: " + o.getTarget() + AnsiTable.getCode("cyan") + " ATTACK: " + o.getAttack() + AnsiTable.getCode("light red") + " PRIORITY: " + o.getPriority());
            }
            this.script.capture(AnsiTable.getCode("light red") + "--------------------------------------------------------------------------\001");
        }

        if ("on".equals(argArray.get(0))) {
            this.script.capture(AnsiTable.getCode("white") + "iKill: Standard KOS is now on.");
            this.standardKOS = true;
        }

        if ("off".equals(argArray.get(0))) {
            this.script.capture(AnsiTable.getCode("white") + "iKill: Standard KOS is now off.");
            this.standardKOS = false;
        }

        if ("enhanced".equals(argArray.get(0))) {
            if (argArray.get(1) != null) {
                if ("on".equals(argArray.get(1))) {
                    this.script.capture(AnsiTable.getCode("white") + "iKill: Enhanced KOS is now on.");
                    this.enhancedKOS = true;
                } else {
                    this.script.capture(AnsiTable.getCode("white") + "iKill: Enhanced KOS is now off.");
                    this.enhancedKOS = false;
                }
            }
        }

        return "";
    }

    public void clearLines(List<String> q) {
        q.stream().forEach((object) -> {
            q.remove(object);
        });
    }

    public void IncomingEvent(ScriptInterface event) {
        Matcher n = nonCombatPrompt.matcher(event.getText());
        Matcher c = combatPrompt.matcher(event.getText());

        if (standardKOS) {
            if (n.find()) {
                promptState = true;
                kosGrab = true;
            } else {
                promptState = false;
            }
            if (c.find()) {
                kosGrab = false;
            }
            if (!promptState && kosGrab) {
                this.nonCombatLines.add(event.getEvent());
            }
            if (promptState) {
                parseNonCombat();
            }
        }

        n.reset();
        c.reset();
    }

    private void parseNonCombat() {
        String highestPriorityName = "";
        String highestPriorityAttack = "";
        Matcher targetMatch = null;
        Matcher enhancedTargetMatch = null;

        for (String s : this.nonCombatLines) {
            targetMatch = standardKOSRegex.matcher(s);
            enhancedTargetMatch = enhancedKOSRegex.matcher(s);
            if (targetMatch.find()) {
                for (kosTarget o : this.targets) {
                    if (targetMatch.group(3).toLowerCase().equals(o.getTarget()) && o.getPriority() < currentPriority) {
                        currentPriority = o.getPriority();
                        highestPriorityName = o.getTarget();
                        highestPriorityAttack = o.getAttack();
                    }
                }
                targetMatch.reset();
            }
            if (this.enhancedKOS) {
                enhancedTargetMatch = enhancedKOSRegex.matcher(s);
                if (enhancedTargetMatch.find()) {
                    for (kosTarget o : this.targets) {
                        if (enhancedTargetMatch.group(1).toLowerCase().equals(o.getTarget()) && o.getPriority() < currentPriority) {
                            currentPriority = o.getPriority();
                            highestPriorityName = o.getTarget();
                            highestPriorityAttack = o.getAttack();
                        }
                    }
                    enhancedTargetMatch.reset();
                }
            }
        }

        if (currentPriority < 101 && standardKOS) {
            attack(highestPriorityName, highestPriorityAttack);
            this.currentPriority = 101;
        }

        clearLines(this.nonCombatLines);
    }

    private void attack(String name, String attack) {
        script.send(attack + " " + name);
    }
}
