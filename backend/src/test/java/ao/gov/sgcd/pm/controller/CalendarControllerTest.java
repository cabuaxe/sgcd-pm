package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.config.JwtTokenProvider;
import ao.gov.sgcd.pm.dto.BlockedDayDTO;
import ao.gov.sgcd.pm.dto.CalendarDTO;
import ao.gov.sgcd.pm.dto.TaskDTO;
import ao.gov.sgcd.pm.entity.BlockType;
import ao.gov.sgcd.pm.entity.TaskStatus;
import ao.gov.sgcd.pm.service.CalendarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarController.class)
@AutoConfigureMockMvc(addFilters = false)
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CalendarService calendarService;

    @Test
    void getCalendar_withMonthAndYear_shouldReturn200() throws Exception {
        TaskDTO taskOnDay = TaskDTO.builder()
                .id(1L)
                .taskCode("S1-001")
                .title("Setup Maven")
                .status(TaskStatus.PLANNED)
                .sessionDate(LocalDate.of(2026, 3, 2))
                .build();

        CalendarDTO calendar = CalendarDTO.builder()
                .year(2026)
                .month(3)
                .days(List.of(
                        CalendarDTO.CalendarDayDTO.builder()
                                .date(LocalDate.of(2026, 3, 1))
                                .dayOfWeek("SUN")
                                .isBlocked(false)
                                .isWorkDay(true)
                                .build(),
                        CalendarDTO.CalendarDayDTO.builder()
                                .date(LocalDate.of(2026, 3, 2))
                                .dayOfWeek("MON")
                                .isBlocked(false)
                                .isWorkDay(true)
                                .task(taskOnDay)
                                .build(),
                        CalendarDTO.CalendarDayDTO.builder()
                                .date(LocalDate.of(2026, 3, 7))
                                .dayOfWeek("SAT")
                                .isBlocked(false)
                                .isWorkDay(false)
                                .build()
                ))
                .build();

        when(calendarService.getCalendar(2026, 3)).thenReturn(calendar);

        mockMvc.perform(get("/v1/calendar")
                        .param("year", "2026")
                        .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year", is(2026)))
                .andExpect(jsonPath("$.month", is(3)))
                .andExpect(jsonPath("$.days", hasSize(3)))
                .andExpect(jsonPath("$.days[0].date", is("2026-03-01")))
                .andExpect(jsonPath("$.days[0].dayOfWeek", is("SUN")))
                .andExpect(jsonPath("$.days[1].task.taskCode", is("S1-001")))
                .andExpect(jsonPath("$.days[2].workDay", is(false)));
    }

    @Test
    void getCalendar_withoutParams_shouldUseCurrentMonthAndYear() throws Exception {
        LocalDate now = LocalDate.now();
        CalendarDTO calendar = CalendarDTO.builder()
                .year(now.getYear())
                .month(now.getMonthValue())
                .days(List.of())
                .build();

        when(calendarService.getCalendar(now.getYear(), now.getMonthValue())).thenReturn(calendar);

        mockMvc.perform(get("/v1/calendar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year", is(now.getYear())))
                .andExpect(jsonPath("$.month", is(now.getMonthValue())));
    }

    @Test
    void getCalendar_withBlockedDay_shouldIncludeBlockInfo() throws Exception {
        CalendarDTO calendar = CalendarDTO.builder()
                .year(2026)
                .month(4)
                .days(List.of(
                        CalendarDTO.CalendarDayDTO.builder()
                                .date(LocalDate.of(2026, 4, 10))
                                .dayOfWeek("FRI")
                                .isBlocked(true)
                                .blockReason("Feriado - Sexta-feira Santa")
                                .isWorkDay(true)
                                .build()
                ))
                .build();

        when(calendarService.getCalendar(2026, 4)).thenReturn(calendar);

        mockMvc.perform(get("/v1/calendar")
                        .param("year", "2026")
                        .param("month", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days[0].blocked", is(true)))
                .andExpect(jsonPath("$.days[0].blockReason", is("Feriado - Sexta-feira Santa")));
    }

    @Test
    void getBlockedDays_shouldReturn200WithBlockedDaysList() throws Exception {
        List<BlockedDayDTO> blockedDays = List.of(
                BlockedDayDTO.builder()
                        .id(1L)
                        .blockedDate(LocalDate.of(2026, 4, 10))
                        .dayOfWeek("FRI")
                        .blockType(BlockType.HOLIDAY)
                        .reason("Sexta-feira Santa")
                        .hoursLost(BigDecimal.valueOf(3.5))
                        .build(),
                BlockedDayDTO.builder()
                        .id(2L)
                        .blockedDate(LocalDate.of(2026, 5, 1))
                        .dayOfWeek("FRI")
                        .blockType(BlockType.HOLIDAY)
                        .reason("Dia do Trabalhador")
                        .hoursLost(BigDecimal.valueOf(3.5))
                        .build()
        );

        when(calendarService.getBlockedDays()).thenReturn(blockedDays);

        mockMvc.perform(get("/v1/calendar/blocked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].blockedDate", is("2026-04-10")))
                .andExpect(jsonPath("$[0].dayOfWeek", is("FRI")))
                .andExpect(jsonPath("$[0].blockType", is("HOLIDAY")))
                .andExpect(jsonPath("$[0].reason", is("Sexta-feira Santa")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].reason", is("Dia do Trabalhador")));
    }

    @Test
    void getBlockedDays_withNoBlocked_shouldReturn200WithEmptyList() throws Exception {
        when(calendarService.getBlockedDays()).thenReturn(List.of());

        mockMvc.perform(get("/v1/calendar/blocked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
