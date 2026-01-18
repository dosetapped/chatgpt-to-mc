package chatgpttomcchat.modid;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenu integration entrypoint.
 * This tells ModMenu which screen to open when "Config" is clicked.
 */
public class ChatGPTToMCChatModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (parent) -> new ChatGPTToMCChatConfigScreen(parent);
    }
}
