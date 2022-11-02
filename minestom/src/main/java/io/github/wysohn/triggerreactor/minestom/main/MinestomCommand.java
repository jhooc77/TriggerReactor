package io.github.wysohn.triggerreactor.minestom.main;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.command.ICommand;
import io.github.wysohn.triggerreactor.core.main.command.ICommandExecutor;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.ITabCompleter;
import io.github.wysohn.triggerreactor.core.manager.trigger.command.PredefinedTabCompleters;
import io.github.wysohn.triggerreactor.minestom.bridge.MinestomCommandSender;
import io.github.wysohn.triggerreactor.minestom.bridge.entity.MinestomPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MinestomCommand extends Command implements ICommand {

    CommandExecutor commandExecutor;
    SuggestionCallback suggestionCallback;

    Argument<String[]> argument;

    public MinestomCommand(String name, String...alias) {
        super("triggerreactor:" + name, Stream.concat(Stream.of(name), Arrays.stream(alias)).toArray(String[]::new));

        argument = ArgumentType.StringArray("args")
                .setSuggestionCallback((sender, context, suggestion) -> {
                    if (suggestionCallback != null)
                        suggestionCallback.apply(sender, context, suggestion);
                });

        setDefaultExecutor((sender, context) -> {
            commandExecutor.apply(sender, context);
        });

        addSyntax((sender, context) -> {
            if (commandExecutor != null)
                commandExecutor.apply(sender, context);
        }, argument);
    }

    @Override
    public void setTabCompleterMap(Map<Integer, Set<ITabCompleter>> tabCompleterMap) {
        suggestionCallback = (sender, context, suggestion) -> {
            String[] args = context.get(argument);
            if(tabCompleterMap == null
                    || tabCompleterMap.get(args.length - 1) == null
                    || tabCompleterMap.get(args.length - 1).size() == 0){
                return;
            }
            Set<ITabCompleter> finalCompleters = new HashSet<>();
            List<String> finalProvideList = new ArrayList<>();

            ConditionTabCompleterIterator:
            for(ITabCompleter tc : tabCompleterMap.get(args.length - 1)){
                if(tc.hasConditionMap()){

                    ArgumentIterator:
                    for(int i = 0; i < args.length; i++){
                        if(!tc.hasCondition(i))
                            continue ArgumentIterator;

                        Pattern pt = tc.getCondition(i);
                        if(!pt.matcher(args[i]).matches()){
                            continue ConditionTabCompleterIterator;
                        }
                    }
                }
                finalCompleters.add(tc);
            }

            FinalTabCompletionIterator:
            for(ITabCompleter finalCompleter : finalCompleters){
                if(finalCompleter.isPreDefinedValue()){
                    List<String> values = handlePreDefined(finalCompleter.getPreDefinedValue());
                    String partial = args[args.length - 1];
                    if (partial.length() < 1) { // provide hint
                        if(finalCompleter.getHint() == null)
                            finalProvideList.addAll(values);
                        else
                            finalProvideList.addAll(finalCompleter.getHint());

                    } else { // provide candidates
                        finalProvideList.addAll(values.stream()
                                .filter(val -> val.toLowerCase().startsWith(partial.toLowerCase()))
                                .collect(Collectors.toList()));
                    }
                }else{
                    String partial = args[args.length - 1];
                    if (partial.length() < 1) { // show hint if nothing is entered yet
                        finalProvideList.addAll(finalCompleter.getHint());
                    } else {
                        finalProvideList.addAll(finalCompleter.getCandidates(partial));
                    }
                }
            }

            for (String s : finalProvideList) {
                suggestion.addEntry(new SuggestionEntry(s));
            }
        };
    }
    private static List<String> handlePreDefined(PredefinedTabCompleters val){
        List<String> returning = new ArrayList<>();
        switch (val){
            case PLAYERS:
                MinecraftServer.getConnectionManager().getOnlinePlayers().forEach((p) -> {
                    returning.add(p.getUsername());
                });
                break;
        }
        return returning;
    }


    @Override
    public void setExecutor(ICommandExecutor executor) {
        commandExecutor = (sender, context) -> {
            String label = context.getCommandName();
            String[] args = context.getOrDefault(argument, new String[0]);
            ICommandSender iSender;

            if(sender instanceof Player player){
                iSender = new MinestomPlayer(player);
            }else{
                iSender = new MinestomCommandSender(sender);
            }

            executor.execute(iSender, label, args);
        };
    }
}
