package ru.exyon.telegrambot.unittests.services;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.exyon.telegrambot.config.BotConfig;
import ru.exyon.telegrambot.core.variables.FirstNameVariable;
import ru.exyon.telegrambot.core.variables.SavedDataVariable;
import ru.exyon.telegrambot.core.variables.Variable;
import ru.exyon.telegrambot.models.DataInfo;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Step;
import ru.exyon.telegrambot.services.DataService;
import ru.exyon.telegrambot.services.VariablesResolverService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class VariablesResolverServiceTest {

    @Mock
    private FirstNameVariable firstNameVariable;

    @Mock
    private SavedDataVariable savedDataVariable;
    @Spy
    private Set<Variable> variables = new HashSet<>();
    @InjectMocks
    @Spy
    private VariablesResolverService variablesResolverService;

    @BeforeEach
    public void beforeEach() {
        if (variables.isEmpty()) {
            variables.add(firstNameVariable);
            variables.add(savedDataVariable);
        }
    }

    //Positive
    @Test
    public void resolveAllVariables_resolveFirstname() {
        Step step = new Step()
                .setId("/start")
                .setText("Привет <<firstname>>!")
                .setNext("/1")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setFirstname("Vova")
                .setStep(step);
        Mockito.when(firstNameVariable.getName()).thenCallRealMethod();
        Mockito.when(firstNameVariable.getValue(Mockito.any(Dialog.class))).thenCallRealMethod();
        Mockito.lenient().when(savedDataVariable.getName()).thenCallRealMethod();

        String resultMessageText = variablesResolverService.resolveAllVariables(dialog);

        Assertions.assertThat(resultMessageText).isEqualTo("Привет Vova!");
    }

    @Test
    public void resolveAllVariables_resolveSavedData() {
        Step step = new Step()
                .setId("/1")
                .setText("<<savedData>>")
                .setNext("/2")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setFirstname("Vova")
                .setStep(step);
        Mockito.lenient().when(firstNameVariable.getName()).thenCallRealMethod();
        Mockito.when(savedDataVariable.getName()).thenCallRealMethod();
        DataInfo dataInfo = new DataInfo()
                .setDate("01.02.2025 11:12")
                .setReason("просто посмотреть");
        Mockito.when(savedDataVariable.getValue(Mockito.any(Dialog.class))).thenReturn(dataInfo.toString());

        String resultMessageText = variablesResolverService.resolveAllVariables(dialog);

        Assertions.assertThat(resultMessageText).isEqualTo(dataInfo.toString());
    }

    @Test
    public void resolveAllVariables_noVariable() {
        Step step = new Step()
                .setId("/start")
                .setText("Привет!")
                .setNext("/1")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setFirstname("Vova")
                .setStep(step);

        String resultMessageText = variablesResolverService.resolveAllVariables(dialog);

        Assertions.assertThat(resultMessageText).isEqualTo(step.getText());
    }

    //Negative
    @Test
    public void resolveAllVariables_emptyVariable() {
        Step step = new Step()
                .setId("/start")
                .setText("Привет <<>>!")
                .setNext("/1")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setFirstname("Vova")
                .setStep(step);

        String resultMessageText = variablesResolverService.resolveAllVariables(dialog);

        Assertions.assertThat(resultMessageText).isEqualTo(step.getText());
    }

    @Test
    public void resolveAllVariables_notDoubleSpecialSymbols() {
        Step step = new Step()
                .setId("/start")
                .setText("Привет <firstname>!")
                .setNext("/1")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setFirstname("Vova")
                .setStep(step);

        String resultMessageText = variablesResolverService.resolveAllVariables(dialog);

        Assertions.assertThat(resultMessageText).isEqualTo(step.getText());
    }

    @Test
    public void resolveAllVariables_noStartSpecialSymbols() {
        Step step = new Step()
                .setId("/start")
                .setText("Привет firstname>>!")
                .setNext("/1")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setFirstname("Vova")
                .setStep(step);

        String resultMessageText = variablesResolverService.resolveAllVariables(dialog);

        Assertions.assertThat(resultMessageText).isEqualTo(step.getText());
    }

    @Test
    public void resolveAllVariables_noEndSpecialSymbols() {
        Step step = new Step()
                .setId("/start")
                .setText("Привет <<firstname!")
                .setNext("/1")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setFirstname("Vova")
                .setStep(step);

        String resultMessageText = variablesResolverService.resolveAllVariables(dialog);

        Assertions.assertThat(resultMessageText).isEqualTo(step.getText());
    }

    @Test
    public void resolveAllVariables_wrongVariableName() {
        Step step = new Step()
                .setId("/start")
                .setText("Привет <<Noname>>!")
                .setNext("/1")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setFirstname("Vova")
                .setStep(step);
        Mockito.when(firstNameVariable.getName()).thenCallRealMethod();
        Mockito.when(savedDataVariable.getName()).thenCallRealMethod();

        String resultMessageText = variablesResolverService.resolveAllVariables(dialog);

        Assertions.assertThat(resultMessageText).isEqualTo(step.getText());
    }

    @Test
    public void resolveAllVariables_twoWrongVariablesNames() {
        Step step = new Step()
                .setId("/start")
                .setText("Привет <<Noname>> <<null>>!")
                .setNext("/1")
                .setNeedToSave(false);
        Dialog dialog = new Dialog()
                .setId(UUID.randomUUID())
                .setFirstname("Vova")
                .setStep(step);
        Mockito.when(firstNameVariable.getName()).thenCallRealMethod();
        Mockito.when(savedDataVariable.getName()).thenCallRealMethod();

        String resultMessageText = variablesResolverService.resolveAllVariables(dialog);

        Assertions.assertThat(resultMessageText).isEqualTo(step.getText());
    }
}
