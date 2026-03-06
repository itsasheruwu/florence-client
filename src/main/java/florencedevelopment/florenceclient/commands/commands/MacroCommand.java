/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.commands.Command;
import florencedevelopment.florenceclient.commands.arguments.MacroArgumentType;
import florencedevelopment.florenceclient.events.world.TickEvent;
import florencedevelopment.florenceclient.systems.macros.Macro;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.TimeArgumentType;

import java.util.ArrayList;
import java.util.List;

public class MacroCommand extends Command {
    public MacroCommand() {
        super("macro", "Allows you to execute macros.");

        FlorenceClient.EVENT_BUS.subscribe(this);
    }

    List<ScheduledMacro> scheduleQueue = new ArrayList<>();
    List<ScheduledMacro> scheduledMacros = new ArrayList<>();

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
            .then(literal("clear")
                .executes(context -> {
                    if (scheduleQueue.isEmpty() && scheduledMacros.isEmpty()) {
                        error("No macros are currently scheduled.");
                        return SINGLE_SUCCESS;
                    }

                    clearAll();
                    info("Cleared all scheduled macros.");

                    return SINGLE_SUCCESS;
                })
                .then(argument("macro", MacroArgumentType.create())
                    .executes(context -> {
                        Macro macro = MacroArgumentType.get(context);

                        if (!isScheduled(macro)) {
                            error("This macro is not currently scheduled.");
                            return SINGLE_SUCCESS;
                        }

                        clear(macro);
                        info("Cleared scheduled macro.");
                        return SINGLE_SUCCESS;
                    })
                )
            )
            .then(argument("macro", MacroArgumentType.create())
                .executes(context -> {
                    Macro macro = MacroArgumentType.get(context);
                    scheduleQueue.add(new ScheduledMacro(0, macro));

                    return SINGLE_SUCCESS;
                })
                .then(argument("delay", TimeArgumentType.time())
                    .executes(context -> {
                        Macro macro = MacroArgumentType.get(context);
                        scheduleQueue.add(new ScheduledMacro(IntegerArgumentType.getInteger(context, "delay"), macro));

                        return SINGLE_SUCCESS;
                    })
                )
            )
        ;
    }

    public void clearAll() {
        scheduleQueue.clear();
        scheduledMacros.clear();
    }

    public boolean isScheduled(Macro macro) {
        return scheduleQueue.stream().anyMatch(element -> element.macro == macro) ||
            scheduledMacros.stream().anyMatch(element -> element.macro == macro);
    }

    public void clear(Macro macro) {
        scheduleQueue.removeIf(scheduledMacro -> scheduledMacro.macro == macro);
        scheduledMacros.removeIf(scheduledMacro -> scheduledMacro.macro == macro);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!scheduleQueue.isEmpty()) {
            scheduledMacros.addAll(scheduleQueue);
            scheduleQueue.clear();
        }

        if (!scheduledMacros.isEmpty()) {
            runMacros();
        }

        scheduledMacros.forEach(ScheduledMacro::tick);
    }

    private void runMacros() {
        scheduledMacros.removeIf(ScheduledMacro::run);
    }
}

class ScheduledMacro {
    public int delay;
    public Macro macro;

    public ScheduledMacro(int tickDelay, Macro scheduledMacro) {
        delay = tickDelay;
        macro = scheduledMacro;
    }

    public void tick() {
       delay--;
    }

    public boolean run() {
        if (delay > 0) return false;

        runMacro();
        return true;
    }

    private void runMacro() {
        if (FlorenceClient.mc.player == null) return;

        macro.onAction();
    }
}
