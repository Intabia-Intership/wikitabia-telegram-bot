package com.intabia.wikibot.services.scenaries.implemetations.wikitabia;

import com.intabia.wikibot.datasavers.ChatData;
import com.intabia.wikibot.datasavers.ChatDataContainer;
import com.intabia.wikibot.dto.telegram.UpdateDto;
import com.intabia.wikibot.dto.wikitabia.ResourceDto;
import com.intabia.wikibot.integration.client.WikitabiaClient;
import com.intabia.wikibot.services.httpsenders.abstractions.TelegramInteraction;
import com.intabia.wikibot.services.scenaries.abstractions.Scenario;
import com.intabia.wikibot.services.scenaries.implemetations.inner.Button;
import com.intabia.wikibot.services.scenaries.implemetations.inner.ButtonsMarkup;
import com.intabia.wikibot.util.Util;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourcePageButtonScenario implements Scenario {
  private final ChatDataContainer chatDataContainer;
  private final TelegramInteraction telegramInteraction;
  private final WikitabiaClient wikitabiaClient;

  @Override
  public void doScenario(UpdateDto update, String botToken) {
    ButtonsMarkup buttonsMarkup = new ButtonsMarkup(new Button[][]{new Button[]{
        new Button("Назад", "ресурсы-"),new Button("Еще", "ресурсы+")
    }});
    long pageNumber = getPageNumberFromChatData(Util.getChatId(update));
    String messageFromUser = Util.getTextFromMessage(update);
    if ("ресурсы+".equals(messageFromUser)) {
      ++pageNumber;
    } else if ("ресурсы-".equals(messageFromUser)){
      --pageNumber;
    }
    List<ResourceDto> resources = wikitabiaClient.getResources((int) pageNumber).getBody();
    if (resources == null || resources.isEmpty()) {
      if (pageNumber < 0) {
        ++pageNumber;
      } else {
        --pageNumber;
      }
      return;
    }
    String messageToUser = Util.convertObjectsToReadableString(resources);
    telegramInteraction.sendMessageToUser(botToken, Util.getChatId(update), messageToUser,
        buttonsMarkup);
    chatDataContainer.addOrUpdateChatData(Util.getChatId(update),
        new ChatData(pageNumber));
  }

  private long getPageNumberFromChatData(String chatId) {
    ChatData chatData = chatDataContainer.getChatData(chatId);
    if (chatData == null) {
      return 0;
    }
    return chatData.getResourcePageNumber();
  }

  @Override
  public String getInvokeMessage() {
    return "ресурсы";
  }
}
