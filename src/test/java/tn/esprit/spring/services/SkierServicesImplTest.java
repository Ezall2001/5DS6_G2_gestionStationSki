import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.spring.entities.Skier;
import tn.esprit.spring.entities.Subscription;
import tn.esprit.spring.repository.SkierRepository;
import tn.esprit.spring.services.SkierServicesImpl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SkierServicesImplTest {

    @Mock
    private SkierRepository skierRepository;

    @InjectMocks
    private SkierServicesImpl skierService;

    private Skier skier;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        skier = new Skier();
        skier.setNumSkier(1L);
        skier.setFirstName("John");
        skier.setLastName("Doe");
        skier.setDateOfBirth(LocalDate.of(1990, 1, 1));
        skier.setCity("Mountainville");

        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setType("Annual");
        skier.setSubscription(subscription);
    }

    @Test
    public void testAddSkier_Success() {
        when(skierRepository.save(skier)).thenReturn(skier);
        
        Skier result = skierService.addSkier(skier);
        
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Mountainville", result.getCity());
        assertNotNull(result.getSubscription());
        verify(skierRepository, times(1)).save(skier);
    }

    @Test
    public void testAddSkier_NullSkier() {
        assertThrows(IllegalArgumentException.class, () -> skierService.addSkier(null), "Skier cannot be null");
        verify(skierRepository, never()).save(null);
    }

    @Test
    public void testRetrieveAllSkiers() {
        Skier skier2 = new Skier();
        skier2.setNumSkier(2L);
        skier2.setFirstName("Jane");
        skier2.setLastName("Smith");
        skier2.setCity("SkiTown");

        List<Skier> skiers = Arrays.asList(skier, skier2);
        when(skierRepository.findAll()).thenReturn(skiers);

        List<Skier> result = skierService.retrieveAllSkiers();

        assertEquals(2, result.size());
        verify(skierRepository, times(1)).findAll();
    }

    @Test
    public void testRetrieveSkierById_Found() {
        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));

        Skier result = skierService.retrieveSkier(1L);

        assertNotNull(result);
        assertEquals(1L, result.getNumSkier());
        verify(skierRepository, times(1)).findById(1L);
    }

    @Test
    public void testRetrieveSkierById_NotFound() {
        when(skierRepository.findById(1L)).thenReturn(Optional.empty());

        Skier result = skierService.retrieveSkier(1L);

        assertNull(result);
        verify(skierRepository, times(1)).findById(1L);
    }

    @Test
    public void testRemoveSkier() {
        doNothing().when(skierRepository).deleteById(1L);

        skierService.removeSkier(1L);

        verify(skierRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testAssignSkierToSubscription() {
        Subscription newSubscription = new Subscription();
        newSubscription.setId(2L);
        newSubscription.setType("Monthly");

        Skier updatedSkier = new Skier();
        updatedSkier.setNumSkier(1L);
        updatedSkier.setFirstName("John");
        updatedSkier.setLastName("Doe");
        updatedSkier.setSubscription(newSubscription);

        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));
        when(skierRepository.save(skier)).thenReturn(updatedSkier);

        Skier result = skierService.assignSkierToSubscription(1L, "Monthly");

        assertNotNull(result);
        assertEquals("Monthly", result.getSubscription().getType());
        verify(skierRepository, times(1)).findById(1L);
        verify(skierRepository, times(1)).save(skier);
    }

    @Test
    public void testRetrieveSkiersBySubscriptionType() {
        List<Skier> skiers = Arrays.asList(skier);
        when(skierRepository.findBySubscriptionType("Annual")).thenReturn(skiers);

        List<Skier> result = skierService.retrieveSkiersBySubscriptionType("Annual");

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
        verify(skierRepository, times(1)).findBySubscriptionType("Annual");
    }

    @Test
    public void testRetrieveSkiersWithPistes() {
        Set<tn.esprit.spring.entities.Piste> pistes = new HashSet<>();
        tn.esprit.spring.entities.Piste piste = new tn.esprit.spring.entities.Piste();
        piste.setNumPiste(1L);
        pistes.add(piste);

        skier.setPistes(pistes);
        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));

        Skier result = skierService.retrieveSkier(1L);

        assertNotNull(result);
        assertEquals(1, result.getPistes().size());
        verify(skierRepository, times(1)).findById(1L);
    }
}
