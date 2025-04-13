package org.example;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static EntityManagerFactory entityManagerFactory;

    public static void main(String[] args) {
        entityManagerFactory = Persistence.createEntityManagerFactory("examplePersistenceUnit");
        initializeData();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. Dodaj maga");
            System.out.println("2. Usuń maga");
            System.out.println("3. Wyświetl wszystkie wieże");
            System.out.println("4. Wyświetl wszystkich magów");
            System.out.println("5. Dodaj wieże");
            System.out.println("6. Usuń wieże");
            System.out.println("7. Wyświetl magów z poziomem większym niż");
            System.out.println("8. Wyświetl wieże niższe niż");
            System.out.println("9. Wyświetl magów z wieży z poziomem wyższym niż");
            System.out.println("10. Wyjście");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addMage(scanner);
                    break;
                case 2:
                    deleteMage(scanner);
                    break;
                case 3:
                    displayTowers();
                    break;
                case 4:
                    displayMages();
                    break;
                case 5:
                    addTower(scanner);
                    break;
                case 6:
                    deleteTower(scanner);
                    break;
                case 7:
                    System.out.println("Podaj minimalny poziom maga:");
                    int level = scanner.nextInt();
                    scanner.nextLine(); // konsumujemy nową linię
                    displayMagesWithLevelGreaterThan(entityManagerFactory.createEntityManager(), level);
                    break;
                case 8:
                    System.out.println("Podaj maksymalną wysokość wieży:");
                    int height = scanner.nextInt();
                    scanner.nextLine(); // konsumujemy nową linię
                    displayTowersShorterThan(entityManagerFactory.createEntityManager(), height);
                    break;
                case 9:
                    System.out.println("Podaj nazwę wieży:");
                    String towerName = scanner.nextLine();
                    System.out.println("Podaj minimalny poziom maga:");
                    int mageLevel = scanner.nextInt();
                    scanner.nextLine(); // konsumujemy nową linię
                    displayMagesFromTowerWithLevelGreaterThan(entityManagerFactory.createEntityManager(), towerName, mageLevel);
                    break;
                case 10:
                    entityManagerFactory.close();
                    return;
                default:
                    System.out.println("Nieznana opcja, spróbuj ponownie.");
            }
        }
    }

    private static void addMage(Scanner scanner) {
        System.out.println("Podaj nazwę maga:");
        String mageName = scanner.nextLine();
        System.out.println("Podaj poziom maga:");
        int mageLevel = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Podaj nazwę wieży dla maga (lub pozostaw puste):");
        String towerName = scanner.nextLine();

        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();

        Tower tower = null;
        if (!towerName.isEmpty()) {
            tower = em.find(Tower.class, towerName);
            if (tower == null) {
                System.out.println("Nie znaleziono wieży o nazwie: " + towerName + ". Mag nie zostanie przypisany do wieży.");
                em.getTransaction().rollback();
                em.close();
                return;
            }
        }

        Mage mage = new Mage(mageName, mageLevel, tower);

        em.persist(mage);

        if (tower != null) {
            tower.getMages().add(mage);
            em.persist(tower);
        }

        em.getTransaction().commit();
        em.close();

        System.out.println("Maga dodano pomyślnie.");
    }


    private static void deleteMage(Scanner scanner) {
        System.out.println("Podaj nazwę maga do usunięcia:");
        String mageName = scanner.nextLine();

        EntityManager em = entityManagerFactory.createEntityManager();
        Mage mage = em.find(Mage.class, mageName);
        if (mage != null) {
            em.getTransaction().begin();
            em.remove(mage);
            em.getTransaction().commit();
            System.out.println("Maga usunięto.");
        } else {
            System.out.println("Nie znaleziono maga o podanej nazwie.");
        }
        em.close();
    }

    private static void addTower(Scanner scanner) {
        System.out.println("Podaj nazwę wieży:");
        String towerName = scanner.nextLine();
        System.out.println("Podaj wysokość wieży:");
        int towerHeight = scanner.nextInt();
        scanner.nextLine();

        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();

        Tower tower = new Tower(towerName, towerHeight);

        em.persist(tower);
        em.getTransaction().commit();
        em.close();

        System.out.println("Wieżę dodano pomyślnie.");
    }

    private static void deleteTower(Scanner scanner) {
        System.out.println("Podaj nazwę wieży do usunięcia:");
        String towerName = scanner.nextLine();

        EntityManager em = entityManagerFactory.createEntityManager();
        Tower tower = em.find(Tower.class, towerName);
        if (tower != null) {
            em.getTransaction().begin();
            em.remove(tower);
            em.getTransaction().commit();
            System.out.println("Wieżę usunięto.");
        } else {
            System.out.println("Nie znaleziono wieży o podanej nazwie.");
        }
        em.close();
    }

    private static void initializeData() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();

            // Tworzenie wież
            Tower tower1 = new Tower("Wieża Północna", 50);
            Tower tower2 = new Tower("Wieża Południowa", 45);

            em.persist(tower1);
            em.persist(tower2);

            Mage mage1 = new Mage("Merlin", 20, tower1);
            Mage mage2 = new Mage("Gandalf", 18, tower2);
            Mage mage3 = new Mage("Saruman", 15, tower1);
            Mage mage4 = new Mage("Radagast", 12, tower2);
            Mage mage5 = new Mage("Alatar", 17, tower1);

            em.persist(mage1);
            em.persist(mage2);
            em.persist(mage3);
            em.persist(mage4);
            em.persist(mage5);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }


    private static void displayTowers() {
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Tower> towers = em.createQuery("SELECT t FROM Tower t", Tower.class).getResultList();
        for (Tower tower : towers) {
            System.out.println(tower.getName() + " - wysokość: " + tower.getHeight());
        }
        em.close();
    }

    private static void displayMages() {
        EntityManager em = entityManagerFactory.createEntityManager();
        List<Mage> mages = em.createQuery("SELECT m FROM Mage m", Mage.class).getResultList();
        for (Mage mage : mages) {
            System.out.println(mage.getName() + " - poziom: " + mage.getLevel());
        }
        em.close();
    }

    public static void displayMagesWithLevelGreaterThan(EntityManager entityManager, int level) {
        List<Mage> mages = entityManager.createQuery("SELECT m FROM Mage m WHERE m.level > :level", Mage.class)
                .setParameter("level", level).getResultList();
        for (Mage mage : mages) {
            System.out.println(mage.getName() + " - poziom: " + mage.getLevel());
        }
    }

    public static void displayTowersShorterThan(EntityManager entityManager, int height) {
        List<Tower> towers = entityManager.createQuery("SELECT t FROM Tower t WHERE t.height < :height", Tower.class)
                .setParameter("height", height).getResultList();
        for (Tower tower : towers) {
            System.out.println(tower.getName() + " - wysokość: " + tower.getHeight());
        }
    }


    public static void displayMagesFromTowerWithLevelGreaterThan(EntityManager entityManager, String towerName, int level) {
        List<Mage> mages = entityManager.createQuery("SELECT m FROM Mage m WHERE m.tower.name = :towerName AND m.level > :level", Mage.class)
                .setParameter("towerName", towerName)
                .setParameter("level", level)
                .getResultList();
        for (Mage mage : mages) {
            System.out.println(mage.getName() + " z wieży " + mage.getTower().getName() + " - poziom: " + mage.getLevel());
        }
    }
}
