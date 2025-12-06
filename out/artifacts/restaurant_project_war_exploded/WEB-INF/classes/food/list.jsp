<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="uz">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Barcha ovqatlar</title>
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container mt-5">
    <h2 class="mb-4">Barcha ovqatlar</h2>

    <!-- Yangi ovqat qo'shish -->
    <div class="card mb-4">
        <div class="card-header bg-primary text-white">
            <strong>Yangi ovqat qo'shish</strong>
        </div>
        <div class="card-body">
            <form id="createFoodForm" action="/food" method="post" enctype="multipart/form-data" autocomplete="off">
                <input type="hidden" name="action" value="create">
                <div class="row g-3">
                    <div class="col-md-3">
                        <label class="form-label">Nomi</label>
                        <input type="text" name="name" class="form-control" required>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Tavsif</label>
                        <input type="text" name="description" class="form-control">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">Narxi</label>
                        <input type="number" step="0.01" name="price" class="form-control" required>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">Miqdori</label>
                        <input type="number" name="quantity" class="form-control" required>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">Rasmi</label>
                        <input type="file" name="image" class="form-control" accept="image/*">
                    </div>
                    <div class="col-md-12">
                        <div class="form-check mb-3">
                            <input class="form-check-input" type="checkbox" name="active" value="true" id="create-active" checked>
                            <label class="form-check-label" for="create-active">
                                Faol
                            </label>
                        </div>
                        <div class="d-flex justify-content-end">
                            <button type="submit" class="btn btn-success">
                                <i class="fas fa-plus"></i> Qo'shish
                            </button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- Ovqatlar jadvali -->
    <div class="table-responsive">
        <table class="table table-striped table-hover align-middle">
            <thead class="table-dark">
            <tr>
                <th>ID</th>
                <th>Nomi</th>
                <th>Tavsif</th>
                <th>Narxi</th>
                <th>Miqdori</th>
                <th>Holati</th>
                <th>Amallar</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="food" items="${foods}">
                <tr>
                    <td><c:out value="${food.id}"/></td>
                    <td><c:out value="${food.name}"/></td>
                    <td><c:out value="${food.description}"/></td>
                    <td><c:out value="${food.price}"/></td>
                    <td><c:out value="${food.totalAmount}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${food.active}">
                                <span class="badge bg-success">Faol</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary">Nofaol</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <button type="button" class="btn btn-sm btn-warning me-1" title="Tahrirlash"
                                onclick="showEditModal('${food.id}', '${food.name}', '${food.description}', '${food.price}', '${food.totalAmount}', '${food.active}')">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button type="button" class="btn btn-sm btn-danger" title="O'chirish"
                                onclick="deleteFood('${food.id}')">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <c:if test="${empty foods}">
            <div class="alert alert-info text-center">Hozircha ovqatlar qo'shilmagan.</div>
        </c:if>
    </div>

    <!-- Pagination -->
    <c:if test="${totalPages > 1}">
        <nav aria-label="Sahifalar">
            <ul class="pagination justify-content-center">
                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                    <a class="page-link" href="?page=${currentPage - 1}&size=10&search=${param.search}">Oldingi</a>
                </li>
                <c:forEach begin="1" end="${totalPages}" var="i">
                    <li class="page-item ${currentPage == i ? 'active' : ''}">
                        <a class="page-link" href="?page=${i}&size=10&search=${param.search}">${i}</a>
                    </li>
                </c:forEach>
                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                    <a class="page-link" href="?page=${currentPage + 1}&size=10&search=${param.search}">Keyingi</a>
                </li>
            </ul>
        </nav>
    </c:if>
</div>

<!-- Tahrirlash Modal -->
<div class="modal fade" id="editFoodModal" tabindex="-1" aria-labelledby="editFoodModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="editFoodForm" action="/food" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="update">
                <div class="modal-header">
                    <h5 class="modal-title" id="editFoodModalLabel">Ovqatni tahrirlash</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Yopish"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="id" id="edit-food-id">
                    <div class="mb-3">
                        <label class="form-label">Nomi</label>
                        <input type="text" name="name" id="edit-name" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Tavsif</label>
                        <textarea name="description" id="edit-description" class="form-control" rows="3"></textarea>
                    </div>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Narxi</label>
                            <input type="number" step="0.01" name="price" id="edit-price" class="form-control" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Miqdori</label>
                            <input type="number" name="quantity" id="edit-quantity" class="form-control" required>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Yangi rasm (ixtiyoriy)</label>
                        <input type="file" name="image" class="form-control" accept="image/*">
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" value="true" id="edit-active" name="active" >
                        <label class="form-check-label" for="edit-active">
                            Faol
                        </label>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Yopish</button>
                    <button type="submit" class="btn btn-primary">Saqlash</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- O'chirish formasi -->
<form id="deleteFoodForm" action="/food" method="post" style="display:none;">
    <input type="hidden" name="action" value="delete">
    <input type="hidden" name="id" id="delete-food-id">
</form>

<script>
    function deleteFood(foodId) {
        if (confirm('Rostdan ham ushbu ovqatni o\'chirmoqchimisiz?')) {
            document.getElementById('delete-food-id').value = foodId;
            document.getElementById('deleteFoodForm').submit();
        }
    }

    function showEditModal(id, name, description, price, quantity, active) {
        document.getElementById('edit-food-id').value = id;
        document.getElementById('edit-name').value = name;
        document.getElementById('edit-description').value = description || '';
        document.getElementById('edit-price').value = price;
        document.getElementById('edit-quantity').value = quantity;
        document.getElementById('edit-active').checked = (active === 'true' || active === true);

        var editModal = new bootstrap.Modal(document.getElementById('editFoodModal'));
        editModal.show();
    }
</script>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>