package uz.pdp.restaurantproject.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import uz.pdp.restaurantproject.criteria.BaseCriteria;
import uz.pdp.restaurantproject.model.dto.DataDto;
import uz.pdp.restaurantproject.model.dto.FoodCreateDto;
import uz.pdp.restaurantproject.model.dto.FoodDto;
import uz.pdp.restaurantproject.model.dto.FoodUpdateDto;
import uz.pdp.restaurantproject.service.FoodService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/food")
@MultipartConfig
public class FoodServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(FoodServlet.class.getName());
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final FoodService service = FoodService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int size = parseIntOrDefault(req.getParameter("size"), DEFAULT_PAGE_SIZE);
        int page = parseIntOrDefault(req.getParameter("page"), 0);
        String search = req.getParameter("search");

        BaseCriteria criteria = BaseCriteria.builder()
                .page(page)
                .size(size)
                .search(search)
                .build();

        DataDto<List<FoodDto>> data = service.getAll(criteria);

        req.setAttribute("foods", data.getData());
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", data.getTotalPages());
        req.getRequestDispatcher("/food/list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        switch (action == null ? "" : action) {
            case "delete" -> service.delete(req.getParameter("id"));
            case "update" -> update(req);
            default -> create(req);
        }
        resp.sendRedirect("/food");
    }

    private void create(HttpServletRequest req) throws ServletException, IOException {
        Part image = saveUploadedImage(req);
        service.create(FoodCreateDto.builder()
                .name(req.getParameter("name"))
                .description(req.getParameter("description"))
                .price(parseDouble(req.getParameter("price")))
                .totalAmount(parseInt(req.getParameter("quantity")))
                .active(Boolean.parseBoolean(req.getParameter("active")))
                .image(image)
                .build());
    }

    private void update(HttpServletRequest req) throws ServletException, IOException {
        Part image = saveUploadedImage(req);
        service.update(FoodUpdateDto.builder()
                .name(req.getParameter("name"))
                .description(req.getParameter("description"))
                .price(parseDouble(req.getParameter("price")))
                .totalAmount(parseInt(req.getParameter("quantity")))
                .active(Boolean.parseBoolean(req.getParameter("active")))
                .image(image)
                .build(), req.getParameter("id"));
    }

    /**
     * Writes the uploaded image (if any) to {@code $HOME/uploads} and returns
     * the original Part for downstream metadata, or {@code null} when the form
     * field is empty so the underlying entity keeps its current image.
     */
    private static Part saveUploadedImage(HttpServletRequest req) throws ServletException, IOException {
        Part image = req.getPart("image");
        if (image == null || image.getSize() <= 0) {
            return null;
        }
        String fileName = Paths.get(image.getSubmittedFileName()).getFileName().toString();
        File uploads = new File(System.getProperty("user.home") + "/uploads");
        if (!uploads.exists() && !uploads.mkdirs()) {
            log.log(Level.WARNING, "Could not create uploads directory: {0}", uploads.getAbsolutePath());
        }
        image.write(new File(uploads, fileName).getAbsolutePath());
        return image;
    }

    private static int parseIntOrDefault(String raw, int fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int parseInt(String raw) {
        return Integer.parseInt(raw);
    }

    private static double parseDouble(String raw) {
        return Double.parseDouble(raw);
    }
}
