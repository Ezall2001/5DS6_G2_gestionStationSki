package tn.esprit.spring.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.spring.entities.*;
import tn.esprit.spring.repositories.*;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class SkierServicesImplTest {

    @Mock
    private ISkierRepository skierRepository;

    @Mock
    private ISubscriptionRepository subscriptionRepository;

    @Mock
    private ICourseRepository courseRepository;

    @Mock
    private IRegistrationRepository registrationRepository;

    @Mock
    private IPisteRepository pisteRepository;

    @InjectMocks
    private SkierServicesImpl skierServices;

    private Skier skier;
    private Subscription subscription;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        skier = new Skier();
        skier.setNumSkier(1L);
        skier.setFirstName("John");
        skier.setLastName("Doe");

        subscription = new Subscription();
        subscription.setNumSub(1L);
        subscription.setStartDate(LocalDate.now());
        subscription.setTypeSub(TypeSubscription.ANNUAL);
    }

    // Test for retrieveAllSkiers method
    @Test
    public void testRetrieveAllSkiers() {
        List<Skier> skiers = Arrays.asList(skier);
        when(skierRepository.findAll()).thenReturn(skiers);

        List<Skier> retrievedSkiers = skierServices.retrieveAllSkiers();

        assertNotNull(retrievedSkiers);
        assertEquals(1, retrievedSkiers.size());

        verify(skierRepository, times(1)).findAll();
    }

    @Test
    public void testRetrieveAllSkiers_EmptyList() {
        when(skierRepository.findAll()).thenReturn(new ArrayList<>());

        List<Skier> retrievedSkiers = skierServices.retrieveAllSkiers();

        assertTrue(retrievedSkiers.isEmpty());

        verify(skierRepository, times(1)).findAll();
    }

    // Test for addSkier method annual
    @Test
    public void testAddSkier_AnnualSubscription() {
        skier.setSubscription(subscription);
        when(skierRepository.save(skier)).thenReturn(skier);

        Skier savedSkier = skierServices.addSkier(skier);

        assertNotNull(savedSkier);
        assertEquals(LocalDate.now().plusYears(1), savedSkier.getSubscription().getEndDate());

        verify(skierRepository, times(1)).save(skier);
    }
    //semestriel
    @Test
    public void testAddSkier_SemestrielSubscription() {
        subscription.setTypeSub(TypeSubscription.SEMESTRIEL);
        skier.setSubscription(subscription);
        when(skierRepository.save(skier)).thenReturn(skier);

        Skier savedSkier = skierServices.addSkier(skier);

        assertNotNull(savedSkier);
        assertEquals(LocalDate.now().plusMonths(6), savedSkier.getSubscription().getEndDate());

        verify(skierRepository, times(1)).save(skier);
    }
    //Monthly
    @Test
    public void testAddSkier_MonthlySubscription() {
        subscription.setTypeSub(TypeSubscription.MONTHLY);
        skier.setSubscription(subscription);
        when(skierRepository.save(skier)).thenReturn(skier);

        Skier savedSkier = skierServices.addSkier(skier);

        assertNotNull(savedSkier);
        assertEquals(LocalDate.now().plusMonths(1), savedSkier.getSubscription().getEndDate());

        verify(skierRepository, times(1)).save(skier);
    }
    //test add skier where the subscription exists but getTypeSub() is null
    @Test
    public void testAddSkier_WithNullTypeSub() {
        // Set up skier with a subscription that has a null TypeSub
        subscription.setTypeSub(null);
        skier.setSubscription(subscription);
        
        // Mock the save operation
        when(skierRepository.save(skier)).thenReturn(skier);

        // Call the method
        Skier savedSkier = skierServices.addSkier(skier);

        // Assert that the skier is saved without setting an end date
        assertNotNull(savedSkier);
        assertNull(savedSkier.getSubscription().getEndDate());

        // Verify save was called once
        verify(skierRepository, times(1)).save(skier);
    }


    // Test for assignSkierToSubscription method
    @Test
    public void testAssignSkierToSubscription() {
        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(skierRepository.save(skier)).thenReturn(skier);

        Skier assignedSkier = skierServices.assignSkierToSubscription(1L, 1L);

        assertNotNull(assignedSkier);
        assertEquals(subscription, assignedSkier.getSubscription());

        verify(skierRepository, times(1)).findById(1L);
        verify(subscriptionRepository, times(1)).findById(1L);
        verify(skierRepository, times(1)).save(skier);
    }
    //assign skier to sub with skier not found
    @Test
    public void testAssignSkierToSubscription_SkierNotFound() {
        when(skierRepository.findById(1L)).thenReturn(Optional.empty());

        Skier assignedSkier = skierServices.assignSkierToSubscription(1L, 1L);

        assertNull(assignedSkier);
        verify(skierRepository, times(1)).findById(1L);
        verify(subscriptionRepository, never()).findById(anyLong());
        verify(skierRepository, never()).save(any(Skier.class));
    }

    // Test for removeSkier method
    @Test
    public void testRemoveSkier() {
        skierServices.removeSkier(1L);
        verify(skierRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testRemoveSkier_SkierNotFound() {
        doThrow(new NoSuchElementException()).when(skierRepository).deleteById(1L);

        assertThrows(NoSuchElementException.class, () -> {
            skierServices.removeSkier(1L);
        });

        verify(skierRepository, times(1)).deleteById(1L);
    }
    //add skier with unsupported type sub
    @Test
    public void testAddSkier_WithUnsupportedTypeSub() {
        // Set up skier with a subscription that has an unsupported TypeSub
        subscription.setTypeSub(null); 
        skier.setSubscription(subscription);

        // Mock the save operation
        when(skierRepository.save(skier)).thenReturn(skier);

        // Call the method
        Skier savedSkier = skierServices.addSkier(skier);

        // Assert that the skier is saved without setting an end date
        assertNotNull(savedSkier);
        assertNull(savedSkier.getSubscription().getEndDate()); // Unsupported types shouldn't set end date

        // Verify save was called once
        verify(skierRepository, times(1)).save(skier);
    }
    //add skier and assign to course with registration
    @Test
    public void testAddSkierAndAssignToCourse_WithRegistrations() {
        // Arrange
        skier.setRegistrations(new HashSet<>(Arrays.asList(new Registration(), new Registration())));
        when(skierRepository.save(skier)).thenReturn(skier);
        when(courseRepository.getById(1L)).thenReturn(new Course());

        // Act
        Skier result = skierServices.addSkierAndAssignToCourse(skier, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(skier, result);  // Ensure the saved skier is returned

        // Verify that the repository save method was called on each registration
        for (Registration registration : skier.getRegistrations()) {
            assertEquals(skier, registration.getSkier());
            assertNotNull(registration.getCourse());
            verify(registrationRepository, times(1)).save(registration);
        }

        // Verify course retrieval and skier save
        verify(courseRepository, times(1)).getById(1L);
        verify(skierRepository, times(1)).save(skier);
    }
    //Assign skier to piste when skier not found
    @Test
    public void testAssignSkierToPiste_SkierNotFound() {
        when(skierRepository.findById(1L)).thenReturn(Optional.empty());

        Skier result = skierServices.assignSkierToPiste(1L, 1L);

        assertNull(result);
        verify(skierRepository, times(1)).findById(1L);
        verify(pisteRepository, never()).findById(anyLong());
        verify(skierRepository, never()).save(any(Skier.class));
    }
    
    //assign skier to piste when piste is null
    @Test
    public void testAssignSkierToPiste_PisteNotFound() {
        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));
        when(pisteRepository.findById(1L)).thenReturn(Optional.empty());

        Skier result = skierServices.assignSkierToPiste(1L, 1L);

        // Check that the method returns without assigning a piste
        assertNotNull(result); // Ensure skier is returned
        assertTrue(result.getPistes() == null || result.getPistes().isEmpty()); // pistes should be empty or null
        verify(skierRepository, times(1)).findById(1L);
        verify(pisteRepository, times(1)).findById(1L);
        verify(skierRepository, never()).save(any(Skier.class)); // save should not be called
    }
    //assign skier to piste when skie and piste are found but pistes are null
   @Test
    public void testAssignSkierToPiste_NullPistes() {
        // Set up the mock behavior
        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));
        when(pisteRepository.findById(1L)).thenReturn(Optional.of(new Piste()));

        // Force pistes to be null to trigger the catch block
        skier.setPistes(null);

        // Call the method
        skierServices.assignSkierToPiste(1L, 1L);

        // Verify that pistes are initialized and contain one element
        assertNotNull(skier.getPistes(), "The pistes set should be initialized and not null");
        assertEquals(1, skier.getPistes().size(), "The pistes set should contain one piste after assignment");

        // Verify that save was called
        verify(skierRepository, times(1)).save(skier);
    }


    // Test for retrieveSkier method
    @Test
    public void testRetrieveSkier() {
        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));

        Skier retrievedSkier = skierServices.retrieveSkier(1L);

        assertNotNull(retrievedSkier);
        assertEquals(skier, retrievedSkier);

        verify(skierRepository, times(1)).findById(1L);
    }

    @Test
    public void testRetrieveSkier_SkierNotFound() {
        when(skierRepository.findById(1L)).thenReturn(Optional.empty());

        Skier retrievedSkier = skierServices.retrieveSkier(1L);

        assertNull(retrievedSkier);
        verify(skierRepository, times(1)).findById(1L);
    }

    // Test for retrieveSkiersBySubscriptionType method
    @Test
    public void testRetrieveSkiersBySubscriptionType() {
        List<Skier> skiers = Arrays.asList(skier);
        when(skierRepository.findBySubscription_TypeSub(TypeSubscription.ANNUAL)).thenReturn(skiers);

        List<Skier> retrievedSkiers = skierServices.retrieveSkiersBySubscriptionType(TypeSubscription.ANNUAL);

        assertNotNull(retrievedSkiers);
        assertEquals(1, retrievedSkiers.size());

        verify(skierRepository, times(1)).findBySubscription_TypeSub(TypeSubscription.ANNUAL);
    }

    @Test
    public void testRetrieveSkiersBySubscriptionType_Empty() {
        when(skierRepository.findBySubscription_TypeSub(TypeSubscription.ANNUAL)).thenReturn(new ArrayList<>());

        List<Skier> retrievedSkiers = skierServices.retrieveSkiersBySubscriptionType(TypeSubscription.ANNUAL);

        assertTrue(retrievedSkiers.isEmpty());

        verify(skierRepository, times(1)).findBySubscription_TypeSub(TypeSubscription.ANNUAL);
    }
    //commentaire de test
    // Edge case for adding a skier with a null subscription
    //To Ensure that adding a skier without a subscription does not cause an error
    @Test
    public void testAddSkier_NullSubscription() {
        skier.setSubscription(null);
        when(skierRepository.save(skier)).thenReturn(skier);

        Skier savedSkier = skierServices.addSkier(skier);

        assertNotNull(savedSkier);
        assertNull(savedSkier.getSubscription());

        verify(skierRepository, times(1)).save(skier);
    }
}