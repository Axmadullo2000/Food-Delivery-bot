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


@WebServlet("/food")
@MultipartConfig
public class FoodServlet extends HttpServlet {
    private final FoodService service = FoodService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sizeString = req.getParameter("size");
        String pageString = req.getParameter("page");
        int size;
        int page;
        if (sizeString == null || pageString == null) {
            size = 10;
            page = 0;
        } else {
            size = Integer.parseInt(sizeString);
            page = Integer.parseInt(pageString);
        }


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

        switch (action) {
            case "delete" -> delete(req);
            case "update" -> update(req);
            default -> create(req);
        }

        resp.sendRedirect("/food");
    }

    private void delete(HttpServletRequest req) {
        String id = req.getParameter("id");
        service.delete(id);
    }

    private void create(HttpServletRequest req) throws ServletException, IOException {
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        Double price = Double.parseDouble(req.getParameter("price"));
        Integer quantity = Integer.parseInt(req.getParameter("quantity"));
        Part image = req.getPart("image");
        Boolean active = Boolean.parseBoolean(req.getParameter("active"));

        String fileName;

        if (image != null && image.getSize() > 0) {
            fileName = Paths.get(image.getSubmittedFileName()).getFileName().toString();
            String userHomeDir = System.getProperty("user.home") + "/uploads";
            File uploads = new File(userHomeDir);
            if (!uploads.exists()) uploads.mkdirs();
            File file = new File(uploads, fileName);
            image.write(file.getAbsolutePath());
        }

        service.create(FoodCreateDto.builder()
                .active(active)
                .name(name)
                .description(description)
                .price(price)
                .totalAmount(quantity)
                .image(image)
                .build());
    }

    private void update(HttpServletRequest req) throws ServletException, IOException {
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        Double price = Double.parseDouble(req.getParameter("price"));
        Integer quantity = Integer.parseInt(req.getParameter("quantity"));
        Part image = req.getPart("image");
        Boolean active = Boolean.parseBoolean(req.getParameter("active"));

        String fileName;

        if (image != null && image.getSize() > 0) {
            fileName = Paths.get(image.getSubmittedFileName()).getFileName().toString();
            String userHomeDir = System.getProperty("user.home") + "/uploads";
            File uploads = new File(userHomeDir);
            if (!uploads.exists()) uploads.mkdirs();
            File file = new File(uploads, fileName);
            image.write(file.getAbsolutePath());
        }

        service.update(FoodUpdateDto.builder()
                .image(image)
                .description(description)
                .price(price)
                .totalAmount(quantity)
                .name(name)
                .active(active)
                .build(), id);
    }
}
