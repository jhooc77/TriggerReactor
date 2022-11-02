/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.minestom.manager;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.AbstractScriptEditManager;
import io.github.wysohn.triggerreactor.minestom.tools.TextUtil;
import io.github.wysohn.triggerreactor.tools.ScriptEditor;
import io.github.wysohn.triggerreactor.tools.ScriptEditor.SaveHandler;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.trait.PlayerEvent;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;

public class ScriptEditManager extends AbstractScriptEditManager {

    private final Map<ScriptEditor.ScriptEditorUser, ScriptEditor> editings = new HashMap<>();
    private final Set<ScriptEditor.ScriptEditorUser> viewingUsage = new HashSet<>();
    private final Set<ScriptEditor.ScriptEditorUser> exitDoublecheck = new HashSet<>();

    private final EventNode<PlayerEvent> node;

    public ScriptEditManager(TriggerReactorCore plugin) {
        super(plugin);
        node = EventNode.type("trg-editor", EventFilter.PLAYER);
        node.addListener(PlayerChatEvent.class, event -> {
            Player sender = event.getPlayer();
            MinestomScriptEditorUser editorUser = new MinestomScriptEditorUser(sender);

            if (editings.containsKey(editorUser)) {
                event.setCancelled(true);
                ScriptEditor editor = editings.get(editorUser);

                if (viewingUsage.remove(editorUser)) {
                    editor.printScript(editorUser);
                    return;
                }

                String arg1 = event.getMessage();

                if (arg1.equals("save")) {
                    try {
                        editor.save();
                    } catch (IOException | ScriptException ex) {
                        plugin.handleException(event, ex);
                    } finally {
                        editings.remove(editorUser);
                        editorUser.sendMessage("&aSaved!");
                        event.getPlayer().eventNode().removeChild(node);
                    }
                    return;
                } else if (arg1.equals("exit")) {
                    if (exitDoublecheck.remove(editorUser)) {
                        editings.remove(editorUser);
                        editorUser.sendMessage("&7Done");
                        event.getPlayer().eventNode().removeChild(node);
                    } else {
                        exitDoublecheck.add(editorUser);
                        editorUser.sendMessage("&6Are you sure to exit? &cUnsaved data will be all discared! "
                                + "&dType &6exit &done more time to confirm.");
                    }
                    return;
                } else if (arg1.equals("il")) {
                    editor.insertNewLine();
                } else if (arg1.equals("dl")) {
                    editor.deleteLine();
                } else if (arg1.length() > 0 && arg1.charAt(0) == 'u') {
                    String[] split = arg1.split(" ");

                    int lines = 1;
                    try {
                        lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
                    } catch (NumberFormatException ex) {
                        plugin.handleException(event, ex);
                    }

                    editor.up(lines);
                } else if (arg1.length() > 0 && arg1.charAt(0) == 'd') {
                    String[] split = arg1.split(" ");

                    int lines = 1;
                    try {
                        lines = split.length > 1 ? Integer.parseInt(split[1]) : 1;
                    } catch (NumberFormatException ex) {
                        plugin.handleException(event, ex);
                    }

                    editor.down(lines);
                } else {
                    if (!exitDoublecheck.remove(editorUser)) {
                        editor.intput(arg1.replaceAll("\\^", " "));
                    }
                }

                editor.printScript(editorUser);
            } else {
                Collection<Player> list = new ArrayList<>(event.getRecipients());
                for (Player receiver : list) {
                    MinestomScriptEditorUser receivingUser = new MinestomScriptEditorUser(receiver);
                    if (editings.containsKey(receivingUser))
                        event.getRecipients().remove(receiver);
                }
            }
        }).addListener(PlayerDisconnectEvent.class, event -> {
            MinestomScriptEditorUser editorUser = new MinestomScriptEditorUser(event.getPlayer());

            editings.remove(editorUser);
            viewingUsage.remove(editorUser);
            exitDoublecheck.remove(editorUser);
        });
    }

    @Override
    public void startEdit(ICommandSender sender, String title, String script, SaveHandler saveHandler) {
        Player player = sender.get();
        MinestomScriptEditorUser editorUser = new MinestomScriptEditorUser(player);

        if (editings.containsKey(editorUser))
            return;

        ScriptEditor editor = new ScriptEditor(title, script, saveHandler);
        editings.put(editorUser, editor);

        editorUser.sendMessage(ScriptEditor.USAGE);
        viewingUsage.add(editorUser);

        player.eventNode().addChild(node);
    }

    @Override
    public void reload() {

    }

    @Override
    public void saveAll() {

    }

    private class MinestomScriptEditorUser implements ScriptEditor.ScriptEditorUser {
        private final Player receiver;

        public MinestomScriptEditorUser(Player receiver) {
            this.receiver = receiver;
        }

        @Override
        public void sendMessage(String rawMessage) {
            receiver.sendMessage(TextUtil.coloredAmpersand(rawMessage));
        }

        @Override
        public int hashCode() {
            return receiver.getUuid().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (!(obj instanceof MinestomScriptEditorUser))
                return false;

            MinestomScriptEditorUser other = (MinestomScriptEditorUser) obj;
            return receiver.getUuid().equals(other.receiver.getUuid());
        }
    }
}