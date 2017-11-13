package seedu.address.logic.commands;

import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.customField.CustomField;
import seedu.address.model.customField.UniqueCustomFieldList;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Photo;
import seedu.address.model.person.ReadOnlyPerson;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.person.phone.Phone;
import seedu.address.model.person.phone.UniquePhoneList;
import seedu.address.model.tag.Tag;

//@@author LuLechuan
/**
 * Adds or updates a custom field of a person identified using it's last displayed index from the address book.
 */
public class CustomCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "custom";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Updates one person's custom field identified by the index number used in the last person listing.\n"
            + "Parameters: "
            + "INDEX (must be a positive integer)\n"
            + "CUSTOM_FIELD_NAME \n"
            + "CUSTOM_FIELD_VALUE"
            + "Example: " + COMMAND_WORD + " 1" + " nickname" + " Ah Chuang";

    public static final String MESSAGE_UPDATE_PERSON_CUSTOM_FIELD_SUCCESS = "Updated Person: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";
    public static final String PERSON_NOT_FOUND_EXCEPTION_MESSAGE = "The target person cannot be missing.";

    private final Index targetIndex;

    private final CustomField customField;

    private final Logger logger = LogsCenter.getLogger(CustomCommand.class);

    public CustomCommand(Index targetIndex, CustomField customField) {
        this.targetIndex = targetIndex;
        this.customField = customField;
    }

    /**
     * Adds or Updates a Person's customField
     */
    private Person updatePersonCustomField(ReadOnlyPerson personToUpdateCustomField, CustomField customField) {
        Name name = personToUpdateCustomField.getName();
        Phone phone = personToUpdateCustomField.getPhone();
        Email email = personToUpdateCustomField.getEmail();
        Address address = personToUpdateCustomField.getAddress();
        Photo photo = personToUpdateCustomField.getPhoto();
        UniquePhoneList uniquePhoneList = personToUpdateCustomField.getPhoneList();
        Set<Tag> tags = personToUpdateCustomField.getTags();
        UniqueCustomFieldList customFields = personToUpdateCustomField.getCustomFieldList();
        UniqueCustomFieldList newCustomFields = new UniqueCustomFieldList(customFields.toSet());

        newCustomFields.add(customField);

        Person personUpdated = new Person(name, phone, email, address,
                photo, uniquePhoneList, tags, newCustomFields.toSet());

        return personUpdated;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        List<ReadOnlyPerson> lastShownList = model.getFilteredPersonList();

        logger.info("Get the person of the specified index.");
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            logger.warning(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        ReadOnlyPerson personToUpdateCustomField = lastShownList.get(targetIndex.getZeroBased());
        Person personUpdated = updatePersonCustomField(personToUpdateCustomField, customField);

        try {
            model.updatePerson(personToUpdateCustomField, personUpdated);
        } catch (DuplicatePersonException dpe) {
            logger.warning(MESSAGE_DUPLICATE_PERSON);
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        } catch (PersonNotFoundException pnfe) {
            logger.warning(PERSON_NOT_FOUND_EXCEPTION_MESSAGE);
            throw new AssertionError("The target person cannot be missing");
        }
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(String.format(MESSAGE_UPDATE_PERSON_CUSTOM_FIELD_SUCCESS, personUpdated));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof CustomCommand // instanceof handles nulls
                && this.targetIndex.equals(((CustomCommand) other).targetIndex)
                && this.customField.equals(((CustomCommand) other).customField)); // state check
    }
}
