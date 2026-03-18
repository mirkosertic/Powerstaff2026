package de.mirkosertic.powerstaff.profilesearch.command;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

interface ProfileSearchMessageRepository extends CrudRepository<ProfileSearchMessage, Long> {

    List<ProfileSearchMessage> findByChatId(Long chatId);
}
