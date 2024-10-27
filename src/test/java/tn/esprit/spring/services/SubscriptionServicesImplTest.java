package tn.esprit.spring.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.spring.entities.Skier;
import tn.esprit.spring.entities.Subscription;
import tn.esprit.spring.entities.TypeSubscription;
import tn.esprit.spring.repositories.ISkierRepository;
import tn.esprit.spring.repositories.ISubscriptionRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class SubscriptionServicesImplTest {

    @Mock
    private ISubscriptionRepository subscriptionRepository;

    @Mock
    private ISkierRepository skierRepository;

    @InjectMocks
    private SubscriptionServicesImpl subscriptionServices;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddSubscription() {
        // Mock subscription
        Subscription subscription = new Subscription();
        subscription.setStartDate(LocalDate.now());
        subscription.setTypeSub(TypeSubscription.ANNUAL);

        // Mock repository behavior
        when(subscriptionRepository.save(subscription)).thenReturn(subscription);

        // Test addSubscription method
        Subscription savedSubscription = subscriptionServices.addSubscription(subscription);

        // Verify and assert
        assertNotNull(savedSubscription);
        assertEquals(LocalDate.now().plusYears(1), savedSubscription.getEndDate());
        verify(subscriptionRepository, times(1)).save(subscription);
    }

    @Test
    void testUpdateSubscription() {
        // Mock subscription
        Subscription subscription = new Subscription();
        subscription.setNumSub(1L);

        // Mock repository behavior
        when(subscriptionRepository.save(subscription)).thenReturn(subscription);

        // Test updateSubscription method
        Subscription updatedSubscription = subscriptionServices.updateSubscription(subscription);

        // Verify and assert
        assertNotNull(updatedSubscription);
        assertEquals(1L, updatedSubscription.getNumSub());
        verify(subscriptionRepository, times(1)).save(subscription);
    }

    @Test
    void testRetrieveSubscriptionById() {
        // Mock subscription
        Subscription subscription = new Subscription();
        subscription.setNumSub(1L);

        // Mock repository behavior
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        // Test retrieveSubscriptionById method
        Subscription foundSubscription = subscriptionServices.retrieveSubscriptionById(1L);

        // Verify and assert
        assertNotNull(foundSubscription);
        assertEquals(1L, foundSubscription.getNumSub());
        verify(subscriptionRepository, times(1)).findById(1L);
    }

    @Test
    void testGetSubscriptionByType() {
        // Mock data
        Subscription subscription1 = new Subscription();
        subscription1.setTypeSub(TypeSubscription.ANNUAL);
        Subscription subscription2 = new Subscription();
        subscription2.setTypeSub(TypeSubscription.ANNUAL);
        Set<Subscription> subscriptions = new HashSet<>();
        subscriptions.add(subscription1);
        subscriptions.add(subscription2);

        // Mock repository behavior
        when(subscriptionRepository.findByTypeSubOrderByStartDateAsc(TypeSubscription.ANNUAL))
                .thenReturn(subscriptions);

        // Test getSubscriptionByType method
        Set<Subscription> result = subscriptionServices.getSubscriptionByType(TypeSubscription.ANNUAL);

        // Verify and assert
        assertEquals(2, result.size());
        verify(subscriptionRepository, times(1)).findByTypeSubOrderByStartDateAsc(TypeSubscription.ANNUAL);
    }

    @Test
    void testRetrieveSubscriptionsByDates() {
        // Mock data
        Subscription subscription1 = new Subscription();
        Subscription subscription2 = new Subscription();
        List<Subscription> subscriptions = List.of(subscription1, subscription2);

        // Mock repository behavior
        when(subscriptionRepository.getSubscriptionsByStartDateBetween(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)))
                .thenReturn(subscriptions);

        // Test retrieveSubscriptionsByDates method
        List<Subscription> result = subscriptionServices.retrieveSubscriptionsByDates(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        // Verify and assert
        assertEquals(2, result.size());
        verify(subscriptionRepository, times(1))
                .getSubscriptionsByStartDateBetween(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
    }

    @Test
    void testRetrieveSubscriptions() {
        // Mock data
        Subscription subscription = new Subscription();
        subscription.setNumSub(1L);
        subscription.setEndDate(LocalDate.of(2024, 12, 31));

        Skier skier = new Skier();
        skier.setFirstName("John");
        skier.setLastName("Doe");

        when(subscriptionRepository.findDistinctOrderByEndDateAsc()).thenReturn(List.of(subscription));
        when(skierRepository.findBySubscription(subscription)).thenReturn(skier);

        // Capture logging output
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(subscriptionServices).log.info(logCaptor.capture());

        // Invoke the scheduled method
        subscriptionServices.retrieveSubscriptions();

        // Verify behavior
        verify(subscriptionRepository, times(1)).findDistinctOrderByEndDateAsc();
        verify(skierRepository, times(1)).findBySubscription(subscription);

        // Check log output
        String expectedLog = "1 | 2024-12-31 | John Doe";
        assertEquals(expectedLog, logCaptor.getValue());
    }

    @Test
    void testShowMonthlyRecurringRevenue() {
        // Mock revenue for each subscription type
        when(subscriptionRepository.recurringRevenueByTypeSubEquals(TypeSubscription.MONTHLY)).thenReturn(1000f);
        when(subscriptionRepository.recurringRevenueByTypeSubEquals(TypeSubscription.SEMESTRIEL)).thenReturn(6000f);
        when(subscriptionRepository.recurringRevenueByTypeSubEquals(TypeSubscription.ANNUAL)).thenReturn(12000f);

        // Capture logging output
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(subscriptionServices).log.info(logCaptor.capture());

        // Invoke the scheduled method
        subscriptionServices.showMonthlyRecurringRevenue();

        // Calculate expected revenue
        float expectedRevenue = 1000f + 6000f / 6 + 12000f / 12; // Monthly + half-year + yearly breakdown
        String expectedLog = "Monthly Revenue = " + expectedRevenue;

        // Check that the correct methods were called
        verify(subscriptionRepository, times(1)).recurringRevenueByTypeSubEquals(TypeSubscription.MONTHLY);
        verify(subscriptionRepository, times(1)).recurringRevenueByTypeSubEquals(TypeSubscription.SEMESTRIEL);
        verify(subscriptionRepository, times(1)).recurringRevenueByTypeSubEquals(TypeSubscription.ANNUAL);

        // Check log output
        assertEquals(expectedLog, logCaptor.getValue());
    }
}
