package hiccup.hiccupstore.product.controller;

import hiccup.hiccupstore.commonutil.file.FileStore;
import hiccup.hiccupstore.commonutil.file.UploadFile;
import hiccup.hiccupstore.product.dto.Product;
import hiccup.hiccupstore.product.dto.ProductAddForm;
import hiccup.hiccupstore.product.dto.ProductImage;
import hiccup.hiccupstore.product.dto.page.Page;
import hiccup.hiccupstore.product.service.ProductService;
//import hiccup.hiccupstore.product.util.FileStore;
import hiccup.hiccupstore.product.util.ImageType;
//import hiccup.hiccupstore.product.util.UploadFile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProductManageController {
    private final ProductService productService;
    private final FileStore fileStore;

    @GetMapping("/product/write")
    public String productWrite(){
        return "product/productWrite";
    }

    @PostMapping("/product/write")
    public String productWrite(@ModelAttribute ProductAddForm productAddForm,
                             @RequestParam("productImageFile")MultipartFile productImage,
                             @RequestParam("detailImageFile")MultipartFile detailImage) throws IOException
    {
        UploadFile uploadProductFile = fileStore.storeFile(productImage);
        ProductImage uploadProductImage = ProductImage.builder().imageName(uploadProductFile.getStoreFileName())
                        .imagePath(fileStore.getFullPath(uploadProductFile.getStoreFileName()))
                        .imageType(ImageType.PRODUCT.getValue()) .build();

        UploadFile uploadDetailFile = fileStore.storeFile(detailImage);
        ProductImage uploadDetailImage = ProductImage.builder().imageName(uploadDetailFile.getStoreFileName())
                        .imagePath(fileStore.getFullPath(uploadDetailFile.getStoreFileName()))
                        .imageType(ImageType.DETAIL.getValue()) .build();

        productService.addProduct(productAddForm,uploadProductImage,uploadDetailImage);
        return "redirect:/product/detail?pid=???";

    }

    @GetMapping("/product/edit")
    public String productEdit(Model model, @RequestParam("productId") Integer productId){
        model.addAttribute("product", productService.getProductById(productId));
        ArrayList<ProductImage> productImages =productService.getProductImageListById(productId);

        model.addAttribute("productImageName",productImages.stream().
                filter(Image -> Image.getImageType().equals(ImageType.PRODUCT.getValue())).
                findFirst().orElse(new ProductImage()).getImageName());
        model.addAttribute("detailImageName",productImages.stream().
                filter(Image -> Image.getImageType().equals(ImageType.DETAIL.getValue())).
                findFirst().orElse(new ProductImage()).getImageName());

        return ("product/productEdit");
    }

    @PostMapping("/product/edit")
    public String productEdit(@ModelAttribute Product product,
                              @RequestParam(value = "productImageFile",required = false)MultipartFile productImage,
                              @RequestParam(value = "detailImageFile", required = false)MultipartFile detailImage,
                              @RequestParam(value = "PreProductImage",required = false)String preProductImage,
                              @RequestParam(value = "PreDetailImage", required = false)String preDetailImage) throws IOException
    {
        ArrayList<ProductImage> productImages =productService.getProductImageListById(product.getProductId());

        if(null == preProductImage && ""!=productImage.getOriginalFilename()){

            UploadFile uploadProductFile = fileStore.storeFile(productImage);

            ProductImage uploadProductImage = ProductImage.builder().productId(product.getProductId())
                    .imageName(uploadProductFile.getStoreFileName())
                    .imagePath(fileStore.getFullPath(uploadProductFile.getStoreFileName()))
                    .imageType(ImageType.PRODUCT.getValue()) .build();
            productService.editProductImage(uploadProductImage);

            fileStore.deleteFile(productImages.stream().
                    filter(Image -> Image.getImageType().equals(ImageType.PRODUCT.getValue())).
                    findFirst().orElse(new ProductImage()).getImagePath());

        }

        if(null == preDetailImage && ""!=detailImage.getOriginalFilename()){

            UploadFile uploadDetailFile = fileStore.storeFile(detailImage);

            ProductImage uploadDetailImage = ProductImage.builder().productId(product.getProductId())
                    .imageName(uploadDetailFile.getStoreFileName())
                    .imagePath(fileStore.getFullPath(uploadDetailFile.getStoreFileName()))
                    .imageType(ImageType.DETAIL.getValue()) .build();
            productService.editProductImage(uploadDetailImage);

            fileStore.deleteFile(productImages.stream().
                    filter(Image -> Image.getImageType().equals(ImageType.DETAIL.getValue())).
                    findFirst().orElse(new ProductImage()).getImagePath());
        }

        productService.editProduct(product);

        return ("product/productEdit");
    }
    @PostMapping("product/delete")
    public String deleteProduct(Page page,
                                @RequestParam(name = "pid") String productId){
        productService.delProduct(Integer.parseInt(productId));
        return page.getListLink();
    }



    @ResponseBody
    @GetMapping("/productImage/{filename}")
    public Resource viewImage(@PathVariable String filename) throws MalformedURLException {

        return new UrlResource("file:"+fileStore.getFullPath(filename));

    }
}
